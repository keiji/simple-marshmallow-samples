package io.keiji.marshmallowsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

public class BackupActivity extends AppCompatActivity {
    private static final String TAG = BackupActivity.class.getSimpleName();

    private static final String BACKUP_TARGET_FILE_NAME = "file.dat";

    private File mFile;
    private TextView mTextView;
    private Button mDeleteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        mFile = new File(getFilesDir(), BACKUP_TARGET_FILE_NAME);

        mTextView = (TextView) findViewById(R.id.textview);
        if (mFile.exists()) {
            try {
                mTextView.setText(loadFirstLaunchDate());
            } catch (IOException e) {
                Log.d(TAG, "IOException", e);
            }
        } else {
            mTextView.setText("First launch at ");
            try {
                String date = saveFirstLaunchDate();
                mTextView.append(date);
            } catch (IOException e) {
                Log.d(TAG, "IOException", e);
            }
        }

        mDeleteFile = (Button) findViewById(R.id.btn_delete);
        mDeleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile();
                Toast.makeText(getApplicationContext(), BACKUP_TARGET_FILE_NAME + " deleted.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String saveFirstLaunchDate() throws IOException {
        String date = DateFormat.getDateTimeInstance().format(new Date());
        try (OutputStream os = new FileOutputStream(mFile)) {
            os.write(date.getBytes());
            os.flush();
        }
        return date;
    }

    private String loadFirstLaunchDate() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)))) {
            return br.readLine();
        }
    }


    private void deleteFile() {
        mFile.delete();
    }

}
