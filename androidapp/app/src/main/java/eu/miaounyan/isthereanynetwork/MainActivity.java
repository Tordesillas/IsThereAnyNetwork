package eu.miaounyan.isthereanynetwork;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.support.v7.preference.PreferenceManager;
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

import eu.miaounyan.isthereanynetwork.controller.MapActivity;
import eu.miaounyan.isthereanynetwork.controller.PreferencesActivity;
import eu.miaounyan.isthereanynetwork.service.PermissionManager;
import eu.miaounyan.isthereanynetwork.service.background.AlarmReceiver;
import eu.miaounyan.isthereanynetwork.service.background.AlarmReceiverCache;
import eu.miaounyan.isthereanynetwork.service.background.AlarmSetter;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork;
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService;
import eu.miaounyan.isthereanynetwork.model.NetworkState;
import eu.miaounyan.isthereanynetwork.service.location.GPSTracker;
import eu.miaounyan.isthereanynetwork.service.telephony.Network;
import eu.miaounyan.isthereanynetwork.utils.PermittedToast;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static eu.miaounyan.isthereanynetwork.utils.DataUtilities.getCurrentTimeDate;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_CACHE;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.getAlarmReceiverInterval;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.CACHE_INTERVAL;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.getAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.getCacheAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.setAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.setCacheAlarmPendingIntent;

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

    private AlarmSetter alarmSetter;

    /* Permission */
    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST = 100;

    private SharedPreferences prefs;

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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        permissionManager = new PermissionManager();
        alarmSetter = new AlarmSetter(this);
        networkOnCreate(inflater, parent);
        timerScannerOnCreate();
        checkPermissions();
        locationOnCreate(inflater, parent);
        sendOnCreate();

        if (prefs.getBoolean(KEY_PREF_ALARM_RECEIVER, true)) {
            alarmSetter.startAlarm();
        }

        if (prefs.getBoolean(KEY_PREF_ALARM_RECEIVER_CACHE, true)) {
            alarmSetter.startCache();
        }
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
        networkTitle.setText(R.string.network_view_title);
        networkInfo = networkView.findViewById(R.id.info_text_view);
        networkInfo.setText(R.string.no_data_available);
        networkData = networkView.findViewById(R.id.data_text_view);

        network = new Network((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE), this);

        parent.addView(networkView);
    }

    private void locationOnCreate(LayoutInflater inflater, ViewGroup parent) {
        View locationView = inflater.inflate(R.layout.sensor, parent, false);
        gpsTracker = new GPSTracker(this);

        ImageView locationIcon = locationView.findViewById(R.id.sensor_image_view);
        locationIcon.setImageResource(R.drawable.ic_gps_location_icon);

        TextView locationTitle = locationView.findViewById(R.id.sensor_title_text_view);
        locationTitle.setText(R.string.gps_location_view_title);
        locationInfo = locationView.findViewById(R.id.info_text_view);
        locationInfo.setText(R.string.no_data_available);
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
            Snackbar.make(view, "Scanning...", Snackbar.LENGTH_LONG).show();
            launchTimer();
        });
    }

    private void sendOnCreate() {
        isThereAnyNetwork = new IsThereAnyNetwork();
        isThereAnyNetworkService = isThereAnyNetwork.connect();

        findViewById(R.id.send_button).setOnClickListener(view -> sendNetworkState(getApplicationContext()));
    }

    private void sendNetworkState(Context context) {
        if (network.isConsistent() && gpsTracker.isConsistent()) {
            Log.d(this.getClass().getName(), "Sending network state");
            PermittedToast.makeText(context, "Sending...", Toast.LENGTH_LONG).show();
            isThereAnyNetworkService.sendNetworkState(new NetworkState(gpsTracker.getLatitude(), gpsTracker.getLongitude(), network.getSignalStrength(), network.getOperator(), getCurrentTimeDate(), network.getType()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(r -> {
                        Log.d(this.getClass().getName(), "Sent network state");
                        PermittedToast.makeText(context, "Sent " + r.getSignalStrength() + " at lat=" +
                                gpsTracker.getLatitude() + ";lon=" + gpsTracker.getLongitude(), Toast.LENGTH_LONG).show();
                    }, err -> {
                        Log.e(this.getClass().getName(), "Error: " + err);
                        PermittedToast.makeText(context, "Error " + err.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.d(this.getClass().getName(), "Can't send network state, data are missing");
            PermittedToast.makeText(context, "Missing data, check connectivity & GPS", Toast.LENGTH_LONG).show();
        }
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

        /* Location */
        gpsTracker.determineLocation();
        locationData.setText(String.format("Lat: %.6f\nLon: %.6f", gpsTracker.getLatitude(), gpsTracker.getLongitude()));
    }
}