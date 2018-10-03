package eu.miaounyan.isthereanynetwork;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import eu.miaounyan.isthereanynetwork.controller.MapActivity;

public class MainActivity extends AppCompatActivity {
    private ProgressBar timeCountProgressBar;
    private TextView timeCountTextView;
    private final static int SCAN_TIME = 30;
    private int remainingTime;
    private Button scanButton;
    private TelephonyManager telephonyManager;
    private int signalStrength;

    private static final String[] PERMISSIONS = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION };
    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Timer */
        View scrollView = findViewById(R.id.main_page);
        timeCountTextView = scrollView.findViewById(R.id.timeCount);
        timeCountProgressBar = scrollView.findViewById(R.id.timeCount_progress_bar);

        timeCountProgressBar.setMax(SCAN_TIME);
        timeCountProgressBar.setProgress(remainingTime);
        timeCountTextView.setText(SCAN_TIME + "");

        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(view -> launchTimer());

        (findViewById(R.id.fab)).setOnClickListener(view -> launchMap());

        /* Network scan */
        telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        signalStrength = 0;

        /* Permissions */
        checkPermissions();
    }

    private void launchTimer() {
        remainingTime = SCAN_TIME;
        scanButton.setClickable(false);
        CountDownTimer cdt = new CountDownTimer(SCAN_TIME * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                scan();
                remainingTime--;
                timeCountTextView.setText(remainingTime + "");
                timeCountProgressBar.setProgress(SCAN_TIME - remainingTime);
            }

            public void onFinish() {
                timeCountTextView.setText("Done");
                timeCountProgressBar.setProgress(0);
                scanButton.setClickable(true);
            }
        };
        cdt.start();
    }

    private void scan() {
        String operatorName = telephonyManager.getNetworkOperatorName();
        String simOperatorName = telephonyManager.getSimOperatorName();
        String networkType = networkType();

        ((TextView) findViewById(R.id.data)).setText("Network operator: " + operatorName + "\nSim operator: " + simOperatorName +
                "\nSignal strength: " + signalStrength + " dBm\nNetwork type: " + networkType);
    }

    private String networkType() {
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            default: return "Unknown";
        }
    }

    private void launchMap() {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0) {
                boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionGranted) {
                    telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            }
        }
    }

    private void checkPermissions() {
        // Checks the Android version of the device.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canWriteExternalStorage = checkIfAlreadyHavePermission(PERMISSIONS[1]);
            boolean canReadExternalStorage = checkIfAlreadyHavePermission(PERMISSIONS[0]);
            if (!canWriteExternalStorage || !canReadExternalStorage) {
                requestPermissions(PERMISSIONS, PERMISSION_REQUEST);
            } else {
                // Permission was granted.
                telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        } else {
            // Version is below Marshmallow.
            telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
        }
    }

    private boolean checkIfAlreadyHavePermission(String perm) {
        int result = ContextCompat.checkSelfPermission(this, perm);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength sigStrength) {
            super.onSignalStrengthsChanged(sigStrength);
            signalStrength = (2 * sigStrength.getGsmSignalStrength()) - 113; // -> dBm
        }
    }
}