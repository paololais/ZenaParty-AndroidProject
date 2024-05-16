package com.example.zenaparty.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.zenaparty.R;
import com.example.zenaparty.fragments.AddEventFragment;
import com.example.zenaparty.fragments.HomeFragment;
import com.example.zenaparty.fragments.ProfileFragment;
import com.example.zenaparty.services.MyNotificationWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{
    FirebaseAuth auth;
    FirebaseUser user;
    BottomNavigationView bottomNavigationView;
    final HomeFragment homeFragment = new HomeFragment();
    final AddEventFragment addEventFragment = new AddEventFragment();
    final ProfileFragment profileFragment = new ProfileFragment();
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Imposta la lingua dell'applicazione
        setAppLanguage();

        //display actionbar
        Objects.requireNonNull(getSupportActionBar()).show();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_foreground);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
            startActivity(intent);
            finish();
        }

        NotificationChannel channel = new NotificationChannel("zena_party", "zena_party", NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("zena_party");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        schedulePeriodicWorker();
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, homeFragment)
                        .commit();
                return true;

            case R.id.addEvent:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, addEventFragment)
                        .commit();
                return true;

            case R.id.profile:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, profileFragment)
                        .commit();
                return true;
        }
        return false;
    }

    private void schedulePeriodicWorker() {
            PeriodicWorkRequest  periodicWork = new PeriodicWorkRequest.Builder(
                    MyNotificationWorker.class,
                    10, TimeUnit.HOURS)
                    .build();

                WorkManager.getInstance(MainActivity.this).enqueue(periodicWork);
    }

    private void setAppLanguage() {
        // lingua dalle SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String languageCode = preferences.getString("pref_language", null);

        if (languageCode != null) {
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

}