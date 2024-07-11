package com.example.zenaparty.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenaparty.R;
import com.example.zenaparty.activities.LogActivity;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingsFragment extends Fragment {

    RelativeLayout logoutButton;
    RelativeLayout modificaProf;
    RelativeLayout language;
    SwitchCompat notificheSwitch;
    private SharedPreferences sharedPreferences;
    boolean notificheBool = true;
    FirebaseUser user;

    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("SavedValues", Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        logoutButton = view.findViewById(R.id.logoutButton);

        user = auth.getCurrentUser();

        notificheSwitch = view.findViewById(R.id.notificationBool);
        notificheBool = notificheSwitch.isChecked();

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        modificaProf = view.findViewById(R.id.modificaprofilo);
        language = view.findViewById(R.id.language);

        modificaProf.setOnClickListener(view1 -> {
            ModificaProFiloFragment modificaProFiloFragment = new ModificaProFiloFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, modificaProFiloFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        language.setOnClickListener(view2->{
            LanguageFragment languageFragment = new LanguageFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, languageFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        logoutButton.setOnClickListener(view12 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage(R.string.logout_dialog)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        //delete username from shared preferences
                        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("username");
                        editor.apply();

                        Intent intent = new Intent(getActivity(), LogActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                    })
                    .show();

        });

        notificheSwitch.setChecked(sharedPreferences.getBoolean("Notifiche", true));

        notificheSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            notificheBool = b;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Notifiche", notificheBool);
            editor.apply();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("Notifiche", notificheBool);
        editor.apply();
    }
}