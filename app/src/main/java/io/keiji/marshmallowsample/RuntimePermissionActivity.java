package io.keiji.marshmallowsample;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Locale;

public class RuntimePermissionActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_LOCATION = 0x01;

    private LocationListener mLocationUpdateListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            mTextView.setText(String.format(Locale.US, "lat: %f\nlng: %f", location.getLatitude(), location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextView = new TextView(this);
        setContentView(mTextView);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdate();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_PERMISSION_LOCATION) {
            return;
        }

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mTextView.setText("必要なパーミッションがありません");
            return;
        }

        startLocationUpdate();
    }

    private void startLocationUpdate() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        //noinspection ResourceType
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, mLocationUpdateListener);
    }
}
