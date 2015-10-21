package io.keiji.marshmallowsample;

import android.app.KeyguardManager;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class FingerprintActivity extends AppCompatActivity {
    private static final String TAG = FingerprintActivity.class.getSimpleName();

    private static final int REQUEST_CODE_DEVICE_CREDENTIAL = 0x01;

    private static final int REQUEST_PERMISSION_FINGERPRINT = 0x01;
    private static final int AUTH_VALID_DURATION_IN_SECOND = 30;

    private static final String PROVIDER_NAME = "AndroidKeyStore";
    private static final String KEY_NAME = "auth_key";

    private static final String CHARSET = "UTF-8";
    private static final String PLAIN = "これは暗号化されるデータです。" +
            "IDであったりパスワードであったり外に持ち出されたときにそのままの形式で読み取られないように、" +
            "暗号化した状態でストレージに保存します";

    private final CancellationSignal mCancellationSignal = new CancellationSignal();
    private final Handler mHandler = new Handler();

    private KeyStore mKeyStore;

    // 暗号化されたデータ
    private byte[] mEncryptedData;

    // IVデータ - 暗号化のデータの復号に必要
    private byte[] mIvData;

    private TextView mTextView;

    private final FingerprintManager.AuthenticationCallback mAuthenticationCallback
            = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errorString) {
            super.onAuthenticationError(errorCode, errorString);
            Log.d(TAG, "onAuthenticationError:" + errorString);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Log.d(TAG, "onAuthenticationHelp:" + helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Log.d(TAG, "onAuthenticationSucceeded");

            mTextView.append("指紋認証に成功しました\n");
            mTextView.append("復号処理中です...\n");

            try {
                // 復号処理
                byte[] plain = result.getCryptoObject().getCipher().doFinal(mEncryptedData);

                mTextView.append("復号されたデータ: ");
                mTextView.append(new String(plain, CHARSET));
            } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
                Log.d(TAG, e.getClass().getSimpleName(), e);
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.d(TAG, "onAuthenticationFailed:");

            mTextView.append("指紋認証に失敗しました\n");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextView = new TextView(this);
        setContentView(mTextView);

        try {
            mKeyStore = KeyStore.getInstance(PROVIDER_NAME);
            mKeyStore.load(null);

            if (!mKeyStore.containsAlias(KEY_NAME)) {
                generateKey();
            }

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }

        launchDeviceCredential();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            mTextView.setText("ユーザー認証に失敗しました\n");
            return;
        }

        try {
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);

            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.ENCRYPT_MODE, key);

            mEncryptedData = cipher.doFinal(PLAIN.getBytes(CHARSET));
            mIvData = cipher.getIV();

            mTextView.setText("元のデータ: " + PLAIN);
            mTextView.append("\n");
            mTextView.append("暗号化されたデータ(Base64): " + Base64.encodeToString(mEncryptedData, Base64.DEFAULT));
            mTextView.append("\n");

        } catch (InvalidKeyException e) {
            mTextView.append("秘密鍵が復号できませんでした\n");
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
                | NoSuchPaddingException | IOException
                | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }

        launchFingerprint();
    }

    private void launchDeviceCredential() {
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        Intent intent = km.createConfirmDeviceCredentialIntent("秘密鍵を復号します",
                "認証の結果は" + AUTH_VALID_DURATION_IN_SECOND + "秒間有効です");
        startActivityForResult(intent, REQUEST_CODE_DEVICE_CREDENTIAL);
    }

    private void launchFingerprint() {
        try {
            if (scanFingerprint()) {
                mTextView.append("指紋をスキャンして下さい\n");
            }
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException
                | InvalidAlgorithmParameterException | KeyStoreException | InvalidKeyException
                | NoSuchPaddingException | IOException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }
    }

    private boolean scanFingerprint() throws CertificateException, NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException {

        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //noinspection ResourceType
        if (fingerprintManager.isHardwareDetected()) {

            Cipher cipher = getCipherInstance();

            IvParameterSpec ivSpec = new IvParameterSpec(mIvData);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

            //noinspection ResourceType
            fingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0x0, mAuthenticationCallback, mHandler);

            return true;
        } else {
            return false;
        }
    }

    public static Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    private static void generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER_NAME);

            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(AUTH_VALID_DURATION_IN_SECOND)
                    .build());
            keyGenerator.generateKey();

        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }
    }

}
