package com.example.zenaparty.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.OnQRCodeDeletedListener;

public class QRCodeDialogFragment extends DialogFragment {

    private static final String ARG_QR_CODE_BYTE_ARRAY = "qr_code_byte_array";
    private static final String ARG_QR_CODE_MESS = "qr_code_mess";
    private static final String ARG_QR_CODE_ID = "qr_code_id";
    private static final String ARG_IS_CREATOR = "is_creator";

    private static final int REQUEST_WRITE_STORAGE = 112;
    private OnQRCodeDeletedListener listener;


    public static QRCodeDialogFragment newInstance(byte[] qrCodeByteArray, String qrCodeMess, String qrCodeId, boolean isCreator,  OnQRCodeDeletedListener listener) {
        QRCodeDialogFragment fragment = new QRCodeDialogFragment();
        Bundle args = new Bundle();
        args.putByteArray(ARG_QR_CODE_BYTE_ARRAY, qrCodeByteArray);
        args.putString(ARG_QR_CODE_MESS, qrCodeMess);
        args.putString(ARG_QR_CODE_ID, qrCodeId);
        args.putBoolean(ARG_IS_CREATOR, isCreator);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_q_r_code_d_ialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        Button downloadButton = view.findViewById(R.id.downloadButton);
        TextView qrCodeMsgTV = view.findViewById(R.id.qrCodeTV);

        assert getArguments() != null;
        byte[] qrCodeByteArray = getArguments().getByteArray(ARG_QR_CODE_BYTE_ARRAY);
        String qrCodeMess = getArguments().getString(ARG_QR_CODE_MESS);
        String qrCodeId = getArguments().getString(ARG_QR_CODE_ID);
        boolean isCreator = getArguments().getBoolean(ARG_IS_CREATOR);


        qrCodeMsgTV.setText(qrCodeMess);
        qrCodeMsgTV.setMovementMethod(new ScrollingMovementMethod());

        // Ottieni la larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        if(width > 1080) width = 1080;

        // Ricrea il bitmap dal byte array
        Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(qrCodeByteArray, 0, qrCodeByteArray.length);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(qrCodeBitmap, width, width, false);
        qrCodeImageView.setImageBitmap(resizedBitmap);

        //check se siamo nel fragment crea/verifica promozione o in le mie promo -> show/hide deleteButton
        if(isCreator){
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> FirebaseWrapper.Database.DeleteQRCode(qrCodeId, requireContext(), deleted -> {
                if(deleted) {
                    this.dismiss();
                    //aggiornare recyclerview qrcode eliminando la promo tolta
                    if (listener != null) {
                        listener.onQRCodeDeleted(qrCodeId);
                    }
                }
            }));
        } else {
            deleteButton.setVisibility(View.GONE);
        }


        downloadButton.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                saveQRCodeToGallery(resizedBitmap);
            }
        });
    }

    private boolean isStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("QRCodeDialogFragment", "Permesso di scrittura già consentito");
            return true;
        } else {
            Log.v("QRCodeDialogFragment", "Permesso di scrittura non consentito, richiesta permesso");
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("QRCodeDialogFragment", "Permesso di scrittura consentito, procedi con il salvataggio");
            } else {
                Log.v("QRCodeDialogFragment", "Permesso di scrittura non consentito");
                Toast.makeText(getContext(), "Permesso di scrittura non consentito", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveQRCodeToGallery(Bitmap qrCodeBitmap) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                requireActivity().getContentResolver(),
                qrCodeBitmap,
                "QRCode",
                "QR code image"
        );

        if (savedImageURL != null) {
            Toast.makeText(getContext(), "QR Code salvato nella galleria", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Errore durante il salvataggio del QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}