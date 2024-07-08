package com.example.zenaparty.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.QRCodeAdapter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRCodeDialogFragment extends DialogFragment {

    private static final String ARG_QR_CODE_ID = "qr_code_id";
    private static final int REQUEST_WRITE_STORAGE = 112;

    public static QRCodeDialogFragment newInstance(String qrCode) {
        QRCodeDialogFragment fragment = new QRCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QR_CODE_ID, qrCode);
        fragment.setArguments(args);
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

        assert getArguments() != null;
        String qrCodeID = getArguments().getString(ARG_QR_CODE_ID);

        // Ottieni la larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Genera e mostra il codice QR
        Bitmap qrCodeBitmap = QRCodeAdapter.generateQRCode(qrCodeID, width);
        qrCodeImageView.setImageBitmap(qrCodeBitmap);

        // Imposta i listener per i pulsanti
        deleteButton.setOnClickListener(v -> {
            // Logica per eliminare il QR code
            dismiss();
        });

        downloadButton.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                saveQRCodeToGallery(qrCodeBitmap);
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