package com.example.zenaparty.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.EventListAdapter;
import com.example.zenaparty.models.EventListInterface;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;

import java.util.ArrayList;


public class MyEventsFragment extends Fragment  implements EventListInterface {
    ProgressBar progressBar;
    RecyclerView recyclerView;
    EventListAdapter myAdapter;
    ArrayList<MyEvent> list;
    TextView tvNoEvents;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView goBack = view.findViewById(R.id.goBackBtn);
        goBack.setOnClickListener(view1 -> requireActivity().onBackPressed());

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        myAdapter = new EventListAdapter(getContext(),list, this,true);
        recyclerView.setAdapter(myAdapter);
        FirebaseWrapper.Database.getUserEventsInserted(list, myAdapter, progressBar, tvNoEvents);
    }

    @Override
    public void onItemClick(int position) {
        EventOpenedFragment eventOpenedFragment = new EventOpenedFragment();

        // Passiamo i dati dell'evento al fragment EventOpenedFragment utilizzando un Bundle
        MyEvent selectedEvent = myAdapter.getList().get(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable("event", selectedEvent);
        eventOpenedFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, eventOpenedFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onButtonActionClick(int position) {
        MyEvent selectedEvent = myAdapter.getList().get(position);
        FirebaseWrapper.Database.removeFromInsertedEvents(this, selectedEvent, position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onEventRemoved(boolean success, int position) {
        if(success){
            myAdapter.getList().remove(position);
            myAdapter.notifyDataSetChanged();

            if(list.isEmpty()){
                tvNoEvents.setVisibility(View.VISIBLE);
            }
        }
        else {
            return;
        }
    }
}