package eu.miaounyan.isthereanynetwork;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;

import eu.miaounyan.isthereanynetwork.controller.MapActivity;

public class MainActivity extends AppCompatActivity {

    /* Timer Attributes */
    private final static int SCAN_TIME = 30;
    private int remainingTime;

    private FrameLayout scanButtonContainer;
    private FloatingActionButton scanButton;
    private ProgressBar timeCountProgressBar;
    private TextView timeCountTextView;
    private CountDownTimer cdt;

    /* Network Attributes */
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener myPhoneStateListener;
    private int signalStrength;
    private int signalLevel;
    private int networkType;

    /* Network View Attributes */
    private TextView networkInfo;
    private TextView networkData;

    /* Permission */
    private static final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> launchMap());

        networkOnCreate();
        timerScannerOnCreate();
        checkPermissions();
    }

    private void networkOnCreate() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = findViewById(R.id.main_content_linear_layout);
        inflater.inflate(R.layout.sensor, parent);

        TextView networkTitle = findViewById(R.id.sensor_title_text_view);
        networkTitle.setText("Network");
        networkInfo = findViewById(R.id.info_text_view);
        networkInfo.setText("No Data");
        networkData = findViewById(R.id.data_text_view);

        signalStrength = 0;
        myPhoneStateListener = new MyPhoneStateListener();
        telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    private void timerScannerOnCreate() {
        scanButtonContainer = findViewById(R.id.scan_button_container_frame_layout);
        scanButtonContainer.setBackground(new ColorDrawable(Color.TRANSPARENT));

        timeCountTextView = findViewById(R.id.timeCount);
        //timeCountTextView.setVisibility(View.INVISIBLE);
        timeCountProgressBar = findViewById(R.id.timeCount_progress_bar);

        timeCountProgressBar.setMax(SCAN_TIME);
        timeCountProgressBar.setProgress(remainingTime);
        timeCountTextView.setText(SCAN_TIME + "");

        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(view -> {
            Snackbar.make(view, "Scanning...", Snackbar.LENGTH_LONG);
            launchTimer();
        });
    }

    private void checkPermissions() {
        // Checks the Android version of the device.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canReadExternalStorage = checkIfAlreadyHavePermission(PERMISSIONS[0]);
            boolean canReadFineLocation = checkIfAlreadyHavePermission(PERMISSIONS[1]);
            boolean canReadCoarseLocation = checkIfAlreadyHavePermission(PERMISSIONS[2]);

            if (!canReadExternalStorage || !canReadFineLocation || !canReadCoarseLocation) {
                requestPermissions(PERMISSIONS, PERMISSION_REQUEST);
            } else {
                // Permission was granted.
                telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        } else {
            // Version is below Marshmallow.
            telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
        }
    }

    private boolean checkIfAlreadyHavePermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0) {
                boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (permissionGranted) {
                    telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchMap() {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }

    private void launchTimer() {
        remainingTime = SCAN_TIME;
        Animation startFadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);
        startFadeOutAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                scanButtonContainer.setVisibility(View.INVISIBLE);
            }
        });
        scanButtonContainer.startAnimation(startFadeOutAnimation);
        cdt = new CountDownTimer(SCAN_TIME * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                scan();
                remainingTime--;
                timeCountTextView.setText(remainingTime + "");
                timeCountProgressBar.setProgress(SCAN_TIME - remainingTime);
            }

            public void onFinish() {
                timeCountTextView.setText("Done");
                timeCountProgressBar.setProgress(0);
                scanButtonContainer.setVisibility(View.VISIBLE);
            }
        };
        cdt.start();
    }

    private void scan() {
        String networkType = networkType();
        String networkOperator = telephonyManager.getNetworkOperatorName();
        String networkSimOperator = telephonyManager.getSimOperatorName();
        networkInfo.setText(String.format("Network operator - %s \nSim Operator - %s \nNetwork type - %s", networkOperator, networkSimOperator, networkType));
    }

    private String networkType() {
        networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD_SCDMA";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
        }
        throw new RuntimeException("New type of network");
    }

    private int getLTESignalStrength(SignalStrength sigStrength) {
        try {
            Method[] methods = android.telephony.SignalStrength.class.getMethods();

            for (Method mthd : methods) {
                if (mthd.getName().equals("getLteSignalStrength")) {
                    int LTESignalStrength = (Integer) mthd.invoke(sigStrength, new Object[]{});
                    Log.i(this.getClass().getName(), "getLteSignalStrength= " + (LTESignalStrength - 140));
                    return LTESignalStrength;
                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Exception: " + e.toString());
        }

        return 0; // Return appropriate signal strength error value in case of failure
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength sigStrength) {
            super.onSignalStrengthsChanged(sigStrength);

            if (networkType == TelephonyManager.NETWORK_TYPE_CDMA || networkType == TelephonyManager.NETWORK_TYPE_GSM) {
                signalStrength = (2 * sigStrength.getGsmSignalStrength()) - 113; // -> dBm
                Log.d(this.getClass().getName(), "Pure GSM Signal Strength: " + sigStrength.getGsmSignalStrength() +
                        ", Post-Treatment GSM Signal Strength: " + signalStrength);

                try {
                    signalLevel = (Integer) sigStrength.getClass().getMethod("getGsmLevel").invoke(sigStrength);
                    Log.d(this.getClass().getName(), "Signal Strength Level: " + signalLevel);
                } catch (Exception ex) {
                    Log.v("Error", "Couldn't retrieve signal level - " + ex.getMessage());
                }
            } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                try {
                    signalStrength = (Integer) sigStrength.getClass().getMethod("getLteRsrp").invoke(sigStrength);
                    Log.d(this.getClass().getName(), "getLteRsrp: " + signalStrength);

                    signalLevel = (Integer) sigStrength.getClass().getMethod("getLteLevel").invoke(sigStrength);
                    Log.d(this.getClass().getName(), "Signal Strength Level: " + signalLevel);
                } catch (Exception ex) {
                    Log.v("Error", "Couldn't retrieve either signal strength or signal level - " + ex.getMessage());
                }

                // Alternative
                //signalStrength = getLTESignalStrength(sigStrength);
                // There's also getLTEdBm which is a getter of mLteRsrp
                networkData.setText(signalStrength + " dBm\n\nSignal Level: " + signalLevel);
            }
        }
    }
}