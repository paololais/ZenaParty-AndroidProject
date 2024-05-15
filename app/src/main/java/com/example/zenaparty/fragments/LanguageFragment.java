package com.example.zenaparty.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.example.zenaparty.R;

import java.util.Locale;

public class LanguageFragment extends Fragment {
    private static final String PREF_LANGUAGE_KEY = "pref_language";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language, container, false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        RadioGroup radioGroupLanguage = view.findViewById(R.id.languageGroup);

        String savedLanguage = getSavedLanguage();
        if (savedLanguage != null) {
            selectLanguageRadioButton(savedLanguage, radioGroupLanguage);
        }

        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.italianRadioButton:
                   setLocale("it");
                    break;
                case R.id.englishRadioButton:
                   setLocale("en");
                    break;
                case R.id.frenchRadioButton:
                    setLocale("fr");
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
        // Salva lingua nelle SharedPreferences
        saveLanguage(languageCode);

        LanguageFragment languageFragment = new LanguageFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, languageFragment);
        fragmentManager.popBackStack();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void saveLanguage(String languageCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_LANGUAGE_KEY, languageCode);
        editor.apply();
    }
    private String getSavedLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return preferences.getString(PREF_LANGUAGE_KEY, null);
    }
    private void selectLanguageRadioButton(String languageCode, RadioGroup radioGroup) {
        // Seleziona il RadioButton corrispondente al codice della lingua salvato
        switch (languageCode) {
            case "it":
                radioGroup.check(R.id.italianRadioButton);
                break;
            case "en":
                radioGroup.check(R.id.englishRadioButton);
                break;
            case "fr":
                radioGroup.check(R.id.frenchRadioButton);
                break;
        }
    }
}