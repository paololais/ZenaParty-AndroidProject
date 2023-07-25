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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.zenaparty.R;
import com.example.zenaparty.activities.LogActivity;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterFragment extends LogFragment {
    public interface UsernameAvailabilityCallback {
        void onUsernameAvailabilityChecked(boolean isUsernameAvailable);
    }

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
        View externalView = inflater.inflate(R.layout.fragment_register, container, false);

        TextView link = externalView.findViewById(R.id.switchRegisterToLoginLabel);
        link.setOnClickListener(view -> ((LogActivity) RegisterFragment.this.requireActivity()).renderFragment(true));

        Button button = externalView.findViewById(R.id.buttonRegister);
        button.setOnClickListener(view -> {
            EditText email = externalView.findViewById(R.id.etEmail);
            EditText password = externalView.findViewById(R.id.etPassword1);
            EditText password2 = externalView.findViewById(R.id.etPassword2);
            EditText username = externalView.findViewById(R.id.etUsername);
            ProgressBar progressBar = externalView.findViewById(R.id.progressBar);

            // hide keyboard
            InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

            if (email.getText().toString().isEmpty() ||
                    password.getText().toString().isEmpty() ||
                    password2.getText().toString().isEmpty() ||
                    username.getText().toString().isEmpty()) {
                // TODO: Better error handling + remove this hardcoded strings
                email.setError("Email is required");
                password.setError("Password is required");
                password2.setError("Password is required");
                username.setError("Username is required");
                return;
            }

            if (!password.getText().toString().equals(password2.getText().toString())) {
                // TODO: Better error handling + remove this hardcoded strings
                Toast
                        .makeText(RegisterFragment.this.requireActivity(), "Passwords are different", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Verifica la disponibilità dello username
            checkUsernameAvailability(username.getText().toString(), isUsernameAvailable -> {
                if (isUsernameAvailable) {
                    // Lo username è disponibile, procedi con la registrazione
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    auth.signUp(
                            email.getText().toString(),
                            password.getText().toString(),
                            username.getText().toString(),
                            progressBar,
                            FirebaseWrapper.Callback
                                    .newInstance(RegisterFragment.this.requireActivity(),
                                            RegisterFragment.this.callbackName,
                                            RegisterFragment.this.callbackPrms)
                    );
                } else {
                    // Lo username non è disponibile, mostra un errore
                    Toast
                            .makeText(RegisterFragment.this.requireActivity(), "Username chosen is not available", Toast.LENGTH_SHORT)
                            .show();
                    username.setError("Username chosen is not available");
                }
            });
        });

        return externalView;
    }

    public void checkUsernameAvailability(String username, final UsernameAvailabilityCallback callback) {
        DatabaseReference usernamesDbRef = FirebaseDatabase.getInstance().getReference("usernames");

        usernamesDbRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isUsernameAvailable = !snapshot.exists();
                callback.onUsernameAvailabilityChecked(isUsernameAvailable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUsernameAvailabilityChecked(false);
            }
        });
    }
}