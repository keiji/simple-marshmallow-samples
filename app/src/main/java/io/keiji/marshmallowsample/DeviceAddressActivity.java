package io.keiji.marshmallowsample;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class DeviceAddressActivity extends AppCompatActivity {
    private static final String TAG = DeviceAddressActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String btAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        Log.d(TAG, "Bluetooth Address = " + btAddress);

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String wifiAddress = wm.getConnectionInfo().getMacAddress();

        Log.d(TAG, "WiFi Address = " + wifiAddress);

    }
}
