package eu.miaounyan.isthereanynetwork.controller;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import eu.miaounyan.isthereanynetwork.R;

public class PreferencesActivity extends AppCompatActivity {

    public static final int REQUEST_PREFERENCE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_activity);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        setTitle("Preferences");

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferenceFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
