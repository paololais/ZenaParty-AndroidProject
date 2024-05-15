package com.example.zenaparty.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.auth.FirebaseAuth;


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

        ImageButton deleteReviewBtn= view.findViewById(R.id.btnDelete);

        Bundle args = getArguments();
        if (args != null) {
            String hostUserId = args.getString("hostUserId");

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
                if(b){
                    FirebaseWrapper.Database.sendNewRating(hostUserId, ratingBar, ratingValueTV, numberOfReviewsTV, deleteReviewBtn);
                }
            });

            deleteReviewBtn.setVisibility(View.GONE); // Nascondi il pulsante di default
            // Verifica se l'utente corrente ha inserito una recensione per l'host selezionato
            FirebaseWrapper.Database.checkIfUserReviewedHost(hostUserId, auth.getCurrentUser().getUid(), userReviewed -> {
                if (userReviewed) {
                    // Se l'utente ha inserito una recensione, mostra il pulsante deleteReviewBtn
                    deleteReviewBtn.setVisibility(View.VISIBLE);
                }
            });
            deleteReviewBtn.setOnClickListener(v-> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.remove_review)
                        .setPositiveButton("Ok", (dialog, which) -> FirebaseWrapper.Database.removeReview(hostUserId,ratingBar, ratingValueTV, numberOfReviewsTV, deleteReviewBtn))
                        .setNegativeButton("No", (dialog, which) -> {
                            // Se l'utente sceglie di non rimuovere la recensione, chiudi il dialog
                            dialog.dismiss();
                        })
                        .show();
            });
        }
    }
}