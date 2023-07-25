package com.example.zenaparty.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.R;
import com.example.zenaparty.activities.LogActivity;


public class LoginFragment extends LogFragment {
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.initArguments();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            // See: https://developer.android.com/reference/android/view/LayoutInflater#inflate(org.xmlpull.v1.XmlPullParser,%20android.view.ViewGroup,%20boolean)
            View externalView = inflater.inflate(R.layout.fragment_login, container, false);

            TextView link = externalView.findViewById(R.id.switchLoginToRegisterLabel);
            link.setOnClickListener(view -> ((LogActivity)LoginFragment.this.requireActivity()).renderFragment(false));

            Button button = externalView.findViewById(R.id.buttonLogin);
            button.setOnClickListener(view -> {
                EditText email = externalView.findViewById(R.id.etEmail);
                EditText password = externalView.findViewById(R.id.etPassword);
                ProgressBar progressBar = externalView.findViewById(R.id.progressBar);

                // hide keyboard
                InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    email.setError("Email is required");
                    password.setError("Password is required");
                    return;
                }

                // Perform SignIn
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                auth.signIn(
                        email.getText().toString(),
                        password.getText().toString(),
                        progressBar,
                        FirebaseWrapper.Callback
                                .newInstance(LoginFragment.this.requireActivity(),
                                        LoginFragment.this.callbackName,
                                        LoginFragment.this.callbackPrms)
                );
            });

            return externalView;
    }
}