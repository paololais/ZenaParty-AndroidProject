package com.example.zenaparty.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;

public class BonusFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bonus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView bonusTextView = view.findViewById(R.id.tvBonus);
        TextView useBonusTV = view.findViewById(R.id.useBonus);
        ImageView fireworksImg = view.findViewById(R.id.fireworksImg);


        Bundle bundle = getArguments();
        if (bundle != null) {
            String bonus = bundle.getString("bonus");
            if(bonus!=null){
                //first word is the bonus id, the rest is the message
                int i = bonus.indexOf(' ');
                String qrCodeID = bonus.substring(0, i);

                FirebaseWrapper.Database.checkQRCodeValidity(qrCodeID, IsQRValid ->{
                    if (IsQRValid) {
                        fireworksImg.setVisibility(View.VISIBLE);

                        String bonusMessage = bonus.substring(i);
                        bonusTextView.setText(bonusMessage);

                        useBonusTV.setVisibility(View.VISIBLE);
                    } else {
                        bonusTextView.setText(R.string.qr_code_not_valid);
                    }
                });
            }
        }

    }
}