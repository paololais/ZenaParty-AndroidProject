package com.example.zenaparty.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.QRCodeAdapter;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.OnQRCodeDeletedListener;
import com.example.zenaparty.models.QRCodeData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class PersonalQRFragment extends Fragment implements OnQRCodeDeletedListener {
    private ActivityResultLauncher<ScanOptions> qrCodeLauncher;
    private List<QRCodeData> qrDataList;
    private QRCodeAdapter adapter;
    private ProgressBar progressBar;
    private TextView noQrTV;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Scanned Successfully", Toast.LENGTH_SHORT).show();
                FirebaseWrapper.Database.VerifyAndValidateDiscount(result.getContents(), requireContext());
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal_q_r, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView gobackBtn = view.findViewById(R.id.goBackBtn);
        progressBar = view.findViewById(R.id.progressBar);
        noQrTV = view.findViewById(R.id.tvNoQrs);
        ExtendedFloatingActionButton fabCreate = view.findViewById(R.id.fab_create_promo);
        ExtendedFloatingActionButton fabVerify = view.findViewById(R.id.fab_verify_discount);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        LinearLayout linearLayout = view.findViewById(R.id.linLayout);
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            linearLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = linearLayout.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) { // if more than 15% of the screen height, it's probably a keyboard
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.qrRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        qrDataList = new ArrayList<>();
        adapter = new QRCodeAdapter(qrDataList, requireContext(), true, this);
        recyclerView.setAdapter(adapter);

        FirebaseWrapper.Database.GetUserQrCodes(qrDataList,adapter,progressBar,noQrTV);

        gobackBtn.setOnClickListener(view1 -> requireActivity().onBackPressed());
        fabCreate.setOnClickListener(l-> showCreateDialog());
        fabVerify.setOnClickListener((l-> checkCameraPermissionAndLaunch()));
    }

    private void showCreateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.crea_promo);
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.hint_congratulazioni);
        builder.setView(input);

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String qrMessage = input.getText().toString();
            if (!qrMessage.isEmpty()) {
                FirebaseWrapper.Database.CreateQRCode(requireContext(), qrMessage, success->{
                    if(success) FirebaseWrapper.Database.GetUserQrCodes(qrDataList,adapter,progressBar,noQrTV);
                });
            } else {
                input.setError("Compilare il campo");
            }
        });
        builder.setNegativeButton(R.string.annulla, (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            // Permesso già garantito, avvia l'uso della fotocamera
            showCamera();
        }
    }
    private void showCamera() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR Code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);

        qrCodeLauncher.launch(options);
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted-> {
                if(isGranted){
                    showCamera();
                }
            });

    @Override
    public void onQRCodeDeleted(String qrCodeId) {
        // Rimuovere l'elemento dalla lista e notificare l'adapter
        for (int i = 0; i < qrDataList.size(); i++) {
            if (qrDataList.get(i).getQrCodeId().equals(qrCodeId)) {
                qrDataList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }

        // Controllare se la lista è vuota per mostrare il messaggio appropriato
        if (qrDataList.isEmpty()) {
            noQrTV.setVisibility(View.VISIBLE);
        }
    }
}