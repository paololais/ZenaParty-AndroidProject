package com.example.zenaparty.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.QRCodeAdapter;
import com.example.zenaparty.models.FirebaseWrapper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

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
        ImageView goBack = view.findViewById(R.id.goBackBtn);
        ImageView useBonusQrImg = view.findViewById(R.id.UseBonusQR);

        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String bonusId = bundle.getString("bonus");
            if(bonusId!=null){
                try {
                    FirebaseWrapper.Database.ReadQRCode(bonusId,bonusTextView,QRfound->{
                        if (QRfound){
                            fireworksImg.setVisibility(View.VISIBLE);
                            useBonusTV.setVisibility(View.VISIBLE);

                            useBonusTV.setOnClickListener(l->{
                                String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                String useBonusQr = bonusId + " " + currentUserID;
                                Bitmap qrCodeBitmap = QRCodeAdapter.generateQRCode(useBonusQr, 200);
                                if (qrCodeBitmap!=null){
                                    useBonusQrImg.setImageBitmap(qrCodeBitmap);
                                    useBonusQrImg.setVisibility(View.VISIBLE);

                                    useBonusTV.setVisibility(View.GONE);
                                    FirebaseWrapper.Database.AddDiscount(bonusId);
                                } else {
                                    Toast.makeText(requireContext(),"Error while generating QR Code", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e("FirebaseWrapper", "Invalid qrCodeID for Firebase key: " + bonusId);
                    bonusTextView.setText(R.string.qr_not_valid_retry);
                }

            }
        }
    }

}