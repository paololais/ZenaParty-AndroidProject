package com.example.zenaparty.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ModificaProFiloFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_modifica_profilo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText username = view.findViewById(R.id.usernameEt);
        TextView saveBtn = view.findViewById(R.id.saveBtn);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        saveBtn.setOnClickListener(view12 -> {
            // hide keyboard
            InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view12.getWindowToken(), 0);

            if (username.getText().toString().isEmpty()) {
                username.setError("Username is required");
                return;
            }

            checkUsernameAvailability(username.getText().toString(), isUsernameAvailable -> {
                if (isUsernameAvailable) {
                    Log.d("ModificaProfFragment", "Attempting to change username");
                    FirebaseWrapper.Database.modifyUsername(getContext(), username.getText().toString(), progressBar);
                } else {
                    username.setError("Username non disponibile.");
                }
            });
        });
    }

    public interface UsernameAvailabilityCallback {
        void onUsernameAvailabilityChecked(boolean isUsernameAvailable);
    }

    public void checkUsernameAvailability(String username, final ModificaProFiloFragment.UsernameAvailabilityCallback callback) {
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