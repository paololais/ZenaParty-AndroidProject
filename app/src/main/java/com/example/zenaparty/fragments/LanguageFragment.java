package com.example.zenaparty.fragments;

import static androidx.core.app.ActivityCompat.recreate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;

import java.util.Locale;

public class LanguageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        RadioGroup radioGroupLanguage = view.findViewById(R.id.languageGroup);
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.italianRadioButton:
                   setLocale("it");
                   requireActivity().recreate();
                    break;
                case R.id.englishRadioButton:
                   setLocale("en");
                    requireActivity().recreate();
                    break;
                case R.id.frenchRadioButton:
                    setLocale("fr");
                    requireActivity().recreate();
                    break;
            }
        });
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getBaseContext().getResources().updateConfiguration(config, requireActivity().getBaseContext().getResources().getDisplayMetrics());
    }
}