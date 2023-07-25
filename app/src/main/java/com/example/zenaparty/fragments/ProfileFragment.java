package com.example.zenaparty.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;

public class ProfileFragment extends Fragment {
    ImageView settingImageView;
    TextView eventsTV;
    TextView preferTV;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);


    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingImageView = view.findViewById(R.id.settingsiv);
        eventsTV = view.findViewById(R.id.eventstv);
        preferTV = view.findViewById(R.id.prefertv);

        TextView usernameTv = view.findViewById(R.id.usernameTv);

        FirebaseWrapper.Database.getAndSetUsername(usernameTv);

        settingImageView.setOnClickListener(view1 -> {

            SettingsFragment SettingsFragment = new SettingsFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, SettingsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        eventsTV.setOnClickListener(view12 -> {

            MyEventsFragment myEventsFragment = new MyEventsFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, myEventsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        preferTV.setOnClickListener(view13 -> {

            PreferitiFragment preferitiFragment = new PreferitiFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, preferitiFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
}