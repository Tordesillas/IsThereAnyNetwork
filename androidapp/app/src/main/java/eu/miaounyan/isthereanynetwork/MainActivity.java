package eu.miaounyan.isthereanynetwork;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ProgressBar timeCountProgressBar;
    private TextView timeCountTextView;
    private final static int SCAN_TIME = 30;
    private int remainingTime;
    private CountDownTimer cdt;
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TelephonyManager m = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(this.getClass().getName(), m.getSimState() + " " + TelephonyManager.SIM_STATE_READY);

        /* Timer */
        View scrollView = findViewById(R.id.main_page);
        timeCountTextView = scrollView.findViewById(R.id.timeCount);
        timeCountProgressBar = scrollView.findViewById(R.id.timeCount_progress_bar);

        timeCountProgressBar.setMax(SCAN_TIME);
        timeCountProgressBar.setProgress(remainingTime);
        timeCountTextView.setText(SCAN_TIME + "");

        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTimer();
                scan();
            }
        });

        /* Network scan */
        telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        signalStrength = 0;

        checkPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                isPermissionGranted(grantResults);
                return;
            }
        }
    }

    private void isPermissionGranted(int[] grantResults) {
        if (grantResults.length > 0) {
            Boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionGranted) {
                Log.d(this.getClass().getName(), "permission granted");

                telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            } else {
                Log.d(this.getClass().getName(), "permission not granted");
            }
        }
    }

    private boolean checkIfAlreadyhavePermission(String perm) {
        int result = ContextCompat.checkSelfPermission(this, perm);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void checkPermissions() {
        // Checks the Android version of the device.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Boolean canWriteExternalStorage = checkIfAlreadyhavePermission(PERMISSIONS[1]);
            Boolean canReadExternalStorage = checkIfAlreadyhavePermission(PERMISSIONS[0]);
            if (!canWriteExternalStorage || !canReadExternalStorage) {
                requestPermissions(PERMISSIONS, PERMISSION_REQUEST);
            } else {
                Log.d(this.getClass().getName(), "permission granted");

                // Permission was granted.
                telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        } else {
            Log.d(this.getClass().getName(), "permission not needed");
            // Version is below Marshmallow.
            telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
        }
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

    private void launchTimer() {
        remainingTime = SCAN_TIME;
        scanButton.setClickable(false);
        cdt = new CountDownTimer(SCAN_TIME * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.d(this.getClass().getName(), "nbcisize: " + telephonyManager.getNeighboringCellInfo().size());
                Log.d(this.getClass().getName(), "nbcisize: " + telephonyManager.getAllCellInfo().size());



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

        ((TextView) findViewById(R.id.data)).setText("Network operator: " + operatorName + "\nSim operator: " + simOperatorName +
                "\nSignal strength: " + signalStrength + " dBm");
    }


    private void getLTEsignalStrength(SignalStrength sigStrength)
    {
        try
        {
            Method[] methods = android.telephony.SignalStrength.class.getMethods();

            for (Method mthd : methods)
            {
                if (mthd.getName().equals("getLteSignalStrength"))
                {
                    int LTEsignalStrength = (Integer) mthd.invoke(sigStrength, new Object[] {});
                    Log.i(this.getClass().getName(), "getLteSignalStrength = " + (LTEsignalStrength - 140));
                    return;
                }
            }
        }
        catch (Exception e)
        {
            Log.e(this.getClass().getName(), "Exception: " + e.toString());
        }
    }

    public int getSignalStrengthDbm(CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoCdma) {
            return ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoGsm) {
            return ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoLte) {
            return ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoWcdma) {
            return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
        }
        return 0;
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength sigStrength) {
            super.onSignalStrengthsChanged(sigStrength);
            signalStrength = (2 * sigStrength.getGsmSignalStrength()) - 113; // -> dBm
            Log.d(this.getClass().getName(), "sigStrength: " + sigStrength.getGsmSignalStrength() + ", signalStrength: " + signalStrength);


            String ssignal = sigStrength.toString();
            String[] parts = ssignal.split(" ");
            int u = parts.length >= 8 ? Integer.parseInt(parts[8]) : -1; // some devices is 11
            Log.d(this.getClass().getName(), "parts ltedbm: " + (u - 140));

            try {
                int v = (Integer) sigStrength.getClass().getMethod("getLteRsrp").invoke(sigStrength);
                Log.d(this.getClass().getName(), "getltersrp: " + v);
            } catch (Exception ex) {
                Log.v("Error", "getLteDbm=" + ex.getMessage());
            }

            getLTEsignalStrength(sigStrength);
            Log.d(this.getClass().getName(), "miaou" + telephonyManager.getAllCellInfo().stream().filter(CellInfo::isRegistered).map(MainActivity.this::getSignalStrengthDbm).findAny().orElse(1));
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo)
        {
            super.onCellInfoChanged(cellInfo);
            Log.d(this.getClass().getName(), "cellinfolist size: " + cellInfo.size());
        }
    }
}