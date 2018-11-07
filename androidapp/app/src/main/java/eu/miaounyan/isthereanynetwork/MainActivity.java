package eu.miaounyan.isthereanynetwork;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import eu.miaounyan.isthereanynetwork.controller.MapActivity;
import eu.miaounyan.isthereanynetwork.controller.PreferencesActivity;
import eu.miaounyan.isthereanynetwork.service.GPSTracker;
import eu.miaounyan.isthereanynetwork.service.PermissionManager;
import eu.miaounyan.isthereanynetwork.service.background.AlarmReceiver;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.NetworkState;
import eu.miaounyan.isthereanynetwork.service.telephony.Network;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    /* Timer Attributes */
    private final static int SCAN_TIME = 30;
    private int remainingTime;

    private FrameLayout scanButtonContainer;
    private ProgressBar timeCountProgressBar;
    private TextView timeCountTextView;
    private CountDownTimer cdt;

    /* Network View Attributes */
    private TextView networkInfo;
    private TextView networkData;

    /* Location Attributes */
    private GPSTracker gpsTracker;

    /* Location View Attributes */
    private TextView locationInfo;
    private TextView locationData;

    /* Send */
    private IsThereAnyNetwork isThereAnyNetwork;
    private IsThereAnyNetworkService isThereAnyNetworkService;

    private Network network;
    private PermissionManager permissionManager;

    /* Permission */
    private static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MapActivity.class)));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout parent = findViewById(R.id.main_content_linear_layout);

        permissionManager = new PermissionManager();
        networkOnCreate(inflater, parent);
        timerScannerOnCreate();
        checkPermissions();
        locationOnCreate(inflater, parent);
        sendOnCreate();
        startAlarm();
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.menu_preferences:
                // Load preferences activity.
                intent = new Intent(this, PreferencesActivity.class);
                startActivityForResult(intent, PreferencesActivity.REQUEST_PREFERENCE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void networkOnCreate(LayoutInflater inflater, ViewGroup parent) {
        View networkView = inflater.inflate(R.layout.sensor, parent, false);

        ImageView networkIcon = networkView.findViewById(R.id.sensor_image_view);
        networkIcon.setImageResource(R.drawable.ic_antenna_icon);

        TextView networkTitle = networkView.findViewById(R.id.sensor_title_text_view);
        networkTitle.setText("Network");
        networkInfo = networkView.findViewById(R.id.info_text_view);
        networkInfo.setText("No Data");
        networkData = networkView.findViewById(R.id.data_text_view);

        network = new Network((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE));

        parent.addView(networkView);
    }

    private void locationOnCreate(LayoutInflater inflater, ViewGroup parent) {
        View locationView = inflater.inflate(R.layout.sensor, parent, false);
        gpsTracker = new GPSTracker(this);

        ImageView locationIcon = locationView.findViewById(R.id.sensor_image_view);
        locationIcon.setImageResource(R.drawable.ic_gps_location_icon);

        TextView locationTitle = locationView.findViewById(R.id.sensor_title_text_view);
        locationTitle.setText("GPS Location");
        locationInfo = locationView.findViewById(R.id.info_text_view);
        locationInfo.setText("No Data Available");
        locationData = locationView.findViewById(R.id.data_text_view);

        parent.addView(locationView);
    }

    private void timerScannerOnCreate() {
        scanButtonContainer = findViewById(R.id.scan_button_container_frame_layout);
        scanButtonContainer.setBackground(new ColorDrawable(Color.TRANSPARENT));

        timeCountTextView = findViewById(R.id.timeCount);
        timeCountProgressBar = findViewById(R.id.timeCount_progress_bar);

        timeCountProgressBar.setMax(SCAN_TIME);
        timeCountProgressBar.setProgress(remainingTime);
        timeCountTextView.setText(SCAN_TIME + "");

        findViewById(R.id.scan_button).setOnClickListener(view -> {
            Snackbar.make(view, "Scanning...", Snackbar.LENGTH_LONG);
            launchTimer();
        });
    }

    private String getCurrentTimeDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(now);
    }

    private void sendOnCreate() {
        isThereAnyNetwork = new IsThereAnyNetwork();
        isThereAnyNetworkService = isThereAnyNetwork.connect();

        findViewById(R.id.send_button).setOnClickListener(view -> sendNetworkState(getApplicationContext()));
    }

    private void sendNetworkState(Context context) {
        if (checkDataConsistency()) {
            Log.d(this.getClass().getName(), "Sending network state");
            Toast.makeText(context, "Sending...", Toast.LENGTH_LONG).show();
            isThereAnyNetworkService.sendNetworkState(new NetworkState(gpsTracker.getLatitude(), gpsTracker.getLongitude(), network.getSignalStrength(), network.getOperator(), getCurrentTimeDate(), network.getType()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(r -> {
                        Log.d(this.getClass().getName(), "Sent network state");
                        Toast.makeText(context, "Sent " + r.getSignalStrength(), Toast.LENGTH_LONG).show();
                    }, err -> {
                        Log.e(this.getClass().getName(), "Error: " + err);
                        Toast.makeText(context, "Error " + err.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.d(this.getClass().getName(), "Can't send network state, data are missing");
            Toast.makeText(context, "Missing data, check connectivity", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkDataConsistency() {
        return (-180 <= gpsTracker.getLatitude() && gpsTracker.getLatitude() <= 180) &&
                (-180 <= gpsTracker.getLongitude() && gpsTracker.getLongitude() <= 180) &&
                (gpsTracker.getLatitude() != 0 && gpsTracker.getLongitude() != 0) &&
                (-150 <= network.getSignalStrength() && network.getSignalStrength() < -40) &&
                (!"Unknown".equals(network.getType()));
    }

    private void startAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 5 * 60 * 1000; // minimum is 1 minute as of API 22. 5 minutes here.

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        // Checks the Android version of the device.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionManager.checkPermissions(getApplicationContext(), PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSION_REQUEST);
            } else {
                // Permission was granted.
                network.listen(getApplicationContext());
            }
        } else {
            // Version is below Marshmallow.
            network.listen(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0) {
                boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (permissionGranted) {
                    network.listen(getApplicationContext());
                }
            }
        }
    }

    private void launchTimer() {
        remainingTime = SCAN_TIME;
        Animation startFadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);
        startFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

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
        /* Network */
        String networkType = network.getType();
        String networkOperator = network.getOperator();
        networkInfo.setText(String.format("Network operator - %s \nNetwork type - %s", networkOperator, networkType));
        networkData.setText(network.getSignalStrength() + " dBm\n\nSignal Level: " + network.getSignalLevel());

        gpsTracker.determineLocation();
        locationData.setText(String.format("Lat: %.3f\nLon: %.3f", gpsTracker.getLatitude(), gpsTracker.getLongitude()));
    }
}