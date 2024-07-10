package com.example.zenaparty.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.QRCodeAdapter;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.QRCodeData;

import java.util.ArrayList;
import java.util.List;


public class MyDiscountsFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_discounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView gobackBtn = view.findViewById(R.id.goBackBtn);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView noDiscountsTV = view.findViewById(R.id.tvNoDiscounts);

        RecyclerView recyclerView = view.findViewById(R.id.qrRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<QRCodeData> qrDataList = new ArrayList<>();
        QRCodeAdapter adapter = new QRCodeAdapter(qrDataList, requireContext(), false, null);
        recyclerView.setAdapter(adapter);

        gobackBtn.setOnClickListener(view1 -> requireActivity().onBackPressed());

        FirebaseWrapper.Database.GetUserDiscounts(qrDataList, adapter, progressBar, noDiscountsTV);
    }
}