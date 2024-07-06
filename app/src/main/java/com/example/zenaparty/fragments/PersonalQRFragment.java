package com.example.zenaparty.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.EventListAdapter;
import com.example.zenaparty.adapters.QRCodeAdapter;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PersonalQRFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        EditText qrMessageET = view.findViewById(R.id.qr_et);
        TextView savebtn = view.findViewById(R.id.saveBtn);

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

        List<String> qrDataList = new ArrayList<>();
        qrDataList.add("https://example.com/1");
        qrDataList.add("https://example.com/2");
        qrDataList.add("https://example.com/3");

        QRCodeAdapter adapter = new QRCodeAdapter(qrDataList);
        recyclerView.setAdapter(adapter);


        gobackBtn.setOnClickListener(view1 -> requireActivity().onBackPressed());
        savebtn.setOnClickListener(v -> {
            // hide keyboard
            InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            //get data
            String qrMessage = qrMessageET.getText().toString();

            if (qrMessage.isEmpty()) {
                qrMessageET.setError("Compilare questo campo");
                return;
            }
            FirebaseWrapper.Database.CreateQRCode(qrMessage);

        });



    }
}