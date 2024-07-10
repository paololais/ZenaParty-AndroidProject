package com.example.zenaparty.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;

public class ProfileFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback is only called when MyFragment is at least started
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);


    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView settingImageView = view.findViewById(R.id.settingsiv);
        RelativeLayout myEventsRL = view.findViewById(R.id.myeventsRL);
        RelativeLayout myFavoritesRL = view.findViewById(R.id.myfavoritesRL);
        RelativeLayout myQrCodesRL = view.findViewById(R.id.your_qrRL);
        RelativeLayout myDiscountsRL = view.findViewById(R.id.discountsRL);

        TextView usernameTv = view.findViewById(R.id.usernameTv);

        // Ottieni lo username dalle SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            // Se lo username esiste nelle SharedPreferences, impostalo nel TextView
            usernameTv.setText(username);
        } else {
            // Se non esiste, ottienilo dal database e salvalo nelle SharedPreferences
            FirebaseWrapper.Database.getAndSetUsername(usernameTv, requireContext());
        }
        settingImageView.setOnClickListener(view1 -> {
            SettingsFragment SettingsFragment = new SettingsFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, SettingsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        myEventsRL.setOnClickListener(view12 -> {
            MyEventsFragment myEventsFragment = new MyEventsFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, myEventsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        myFavoritesRL.setOnClickListener(view13 -> {
            PreferitiFragment preferitiFragment = new PreferitiFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, preferitiFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        myQrCodesRL.setOnClickListener(view2 -> {
            PersonalQRFragment personalQRFragment = new PersonalQRFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, personalQRFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        myDiscountsRL.setOnClickListener(view3-> {
            MyDiscountsFragment myDiscountsFragment = new MyDiscountsFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, myDiscountsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }
}