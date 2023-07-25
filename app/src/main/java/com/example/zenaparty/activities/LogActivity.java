package com.example.zenaparty.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.zenaparty.fragments.LogFragment;
import com.example.zenaparty.fragments.LoginFragment;
import com.example.zenaparty.R;
import com.example.zenaparty.fragments.RegisterFragment;

import java.util.Objects;

public class LogActivity extends AppCompatActivity{
    private static final String TAG = LogActivity.class.getCanonicalName();

    private FragmentManager fragmentManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        //hide actionbar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // TODO: Render fragment
        // Render fragment
        renderFragment(true);
    }

    // https://developer.android.com/guide/fragments/transactions
    public void renderFragment(boolean isLogin) {
        Fragment fragment;
        if (isLogin) {
            fragment = LogFragment.newInstance(LoginFragment.class, "signinCallback", boolean.class);
        } else {
            fragment = LogFragment.newInstance(RegisterFragment.class, "signinCallback", boolean.class);
        }
        if (this.fragmentManager == null) {
            this.fragmentManager = this.getSupportFragmentManager();
        }

        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();

        // For optimizations -- See: https://developer.android.com/reference/androidx/fragment/app/FragmentTransaction#setReorderingAllowed(boolean)
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.loginRegisterFragment, fragment);

        fragmentTransaction.commit();
    }

    public void signinCallback(boolean result) {
        if (!result) {
            // TODO: Better handling of the error message --> e.g., put in a textview of the activity/fragment
            Toast
                    .makeText(this, "Username or password are not valid", Toast.LENGTH_LONG)
                    .show();
        } else {
            Intent intent = new Intent(this, SplashActivity.class);
            this.startActivity(intent);
            this.finish();
        }
    }
}