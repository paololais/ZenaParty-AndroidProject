package com.example.zenaparty.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;


public class HostFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        TextView username = view.findViewById(R.id.usernameTv);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        TextView ratingValueTV = view.findViewById(R.id.meanValueTV);
        TextView numberOfReviewsTV = view.findViewById(R.id.numberReviewsTV);

        TextView eventsTV = view.findViewById(R.id.eventstv);

        // Recupera l'ID dell'utente host dall'argomento del Bundle
        Bundle args = getArguments();
        if (args != null) {
            String hostUserId = args.getString("hostUserId");

            // Recupera le informazioni dell'utente host dal database Firebase
            FirebaseWrapper.Database.getUsername(hostUserId, username);

            eventsTV.setOnClickListener(view12 -> {
                HostEventsFragment hostEventsFragment = new HostEventsFragment();
                hostEventsFragment.setArguments(args);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flFragment, hostEventsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });

            //set rating value with mean retrieved and calculated from database, based on userId
            FirebaseWrapper.Database.getHostRating(hostUserId, ratingBar, ratingValueTV, numberOfReviewsTV);

            // Verifica se l'ID utente corrente coincide con l'ID dell'host, in tal caso non può dare recensioni
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                String currentUserId = auth.getCurrentUser().getUid();
                if (currentUserId.equals(hostUserId)) {
                    // L'utente attuale è l'host, quindi impostiamo la RatingBar come un indicatore
                    ratingBar.setIsIndicator(true);
                }
            }
            ratingBar.setOnRatingBarChangeListener((ratingBar1, v, b) -> {
                float newRating = ratingBar.getRating();
                FirebaseWrapper.Database.sendNewRating(hostUserId, newRating);
                FirebaseWrapper.Database.getHostRating(hostUserId, ratingBar, ratingValueTV, numberOfReviewsTV);
            });
        }
    }
}