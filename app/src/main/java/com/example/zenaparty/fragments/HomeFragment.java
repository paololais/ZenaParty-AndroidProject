package com.example.zenaparty.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.EventListAdapter;
import com.example.zenaparty.models.EventListInterface;
import com.example.zenaparty.models.FilterDialogListener;
import com.example.zenaparty.models.MyEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment
        implements EventListInterface, FilterDialogListener {
    private TextView tvSelectDate;
    private TextView tvNoEvents;
    private ProgressBar progressBar;
    RecyclerView recyclerView;
    DatabaseReference database;
    EventListAdapter myAdapter;
    ArrayList<MyEvent> list;
    String formattedDate;
    String newFormattedDate;
    private SharedPreferences sharedPreferences;
    private boolean isNewlyCreated = true;
    final Calendar calendar = Calendar.getInstance();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);

    boolean isParty = true, isSagre = true, isMusica = true, isSport = true, isAltro = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSelectDate = view.findViewById(R.id.tvSelectDate);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        Button btnIncreaseDay = view.findViewById(R.id.btnIncreaseDay);
        Button btnDecreaseDay = view.findViewById(R.id.btnDecreaseDay);
        ImageButton btnFilter = view.findViewById(R.id.btnFilter);
        ImageButton btnRefresh = view.findViewById(R.id.btnRefresh);
        progressBar = view.findViewById(R.id.progressBar);

        sharedPreferences = requireActivity().getSharedPreferences("SavedValues", Context.MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        database = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/").getReference("events");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Controlla se ci sono dati di stato salvati
        if (isNewlyCreated) {
            // L'app è stata appena avviata, impostare i valori di default

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMMM", Locale.getDefault());
            formattedDate = dateFormat.format(calendar.getTime());
            tvSelectDate.setText(formattedDate);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            newFormattedDate = newFormat.format(calendar.getTime());
            list = new ArrayList<>();
            myAdapter = new EventListAdapter(getContext(),list, this, false);
            recyclerView.setAdapter(myAdapter);
            readDatabase(database);
        } else {
            // L'app è stata ripresa dalla modalità di background, recuperare lo stato salvato
            formattedDate = sharedPreferences.getString("date", "");
            tvSelectDate.setText(formattedDate);
            newFormattedDate = sharedPreferences.getString("newFormattedDate", "");

            //get filters
            isParty = sharedPreferences.getBoolean("party", true);
            isSagre = sharedPreferences.getBoolean("sagre", true);
            isMusica = sharedPreferences.getBoolean("musica", true);
            isSport = sharedPreferences.getBoolean("sport", true);
            isAltro = sharedPreferences.getBoolean("altro", true);

            Gson gson = new Gson();
            String json = sharedPreferences.getString("eventList", "");
            Type type = new TypeToken<ArrayList<MyEvent>>() {}.getType();
            list = gson.fromJson(json, type);

            if (list == null) {
                list = new ArrayList<>();
                myAdapter = new EventListAdapter(getContext(),list, this, false);
                recyclerView.setAdapter(myAdapter);
                readDatabase(database);
            }

            // Aggiorna l'adattatore con la lista recuperata
            myAdapter = new EventListAdapter(getContext(),list,this, false);
            recyclerView.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();

            filterEventsByType();
        }


        tvSelectDate.setOnClickListener(v -> {

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMMM", Locale.getDefault());
                formattedDate = dateFormat.format(calendar.getTime());
                tvSelectDate.setText(formattedDate);

                // metodo per filtrare gli eventi in base alla data selezionata
                SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                newFormattedDate = newFormat.format(calendar.getTime());
                filterEventsByType();
            },year, month,day);
            dialog.show();

        });
        // Listener per il pulsante per aumentare il giorno
        btnIncreaseDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMMM", Locale.getDefault());
            formattedDate = dateFormat.format(calendar.getTime());
            tvSelectDate.setText(formattedDate);

            // metodo per filtrare gli eventi in base alla data selezionata
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            newFormattedDate = newFormat.format(calendar.getTime());
            filterEventsByType();
        });
        // Listener per il pulsante per decrementare il giorno
        btnDecreaseDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMMM", Locale.getDefault());
            formattedDate = dateFormat.format(calendar.getTime());
            tvSelectDate.setText(formattedDate);

            // metodo per filtrare gli eventi in base alla data selezionata
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            newFormattedDate = newFormat.format(calendar.getTime());
            filterEventsByType();
        });
        btnFilter.setOnClickListener(view13 -> openFilterDialog());

        btnRefresh.setOnClickListener(view12 -> readDatabase(database));
    }

    private void openFilterDialog(){
        FilterDialogFragment dialog = new FilterDialogFragment();
        dialog.setOnInputListener(this);
        dialog.show(requireActivity().getSupportFragmentManager(), "filter dialog");
    }


    @SuppressLint("NotifyDataSetChanged")
    private void filterEventsByDate(String selectedDate) {
        ArrayList<MyEvent> filteredList = new ArrayList<>();

        for (MyEvent event : list) {
            if (event.getDate().equals(selectedDate)) {
                filteredList.add(event);
            }
        }

        myAdapter.setEventList(filteredList);
        myAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterEventsByType() {
        ArrayList<MyEvent> filteredList = new ArrayList<>();

        for (MyEvent event : list) {
            if (event.getDate().equals(newFormattedDate) &&
                    ((isParty && event.getType().equals("Disco e Feste")) ||
                    (isSagre && event.getType().equals("Sagre")) ||
                    (isMusica && event.getType().equals("Musica")) ||
                    (isSport && event.getType().equals("Sport")) ||
                    (isAltro && event.getType().equals("Altro")))) {
                filteredList.add(event);
            } else if (event.getDate().equals(newFormattedDate) &&
                    ((isAltro && !event.getType().equals("Disco e Feste") &&
                            !event.getType().equals("Sagre") &&
                            !event.getType().equals("Musica") &&
                            !event.getType().equals("Sport")))) {
                filteredList.add(event);
            }
        }

        myAdapter.setEventList(filteredList);
        myAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }
    }

        @Override
    public void onResume() {
        super.onResume();

        isNewlyCreated = false;
    }


    @Override
    public void onPause() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //salva text view data
        editor.putString("date", formattedDate);
        editor.putString("newFormattedDate", newFormattedDate);
        //salva lista eventi corrente
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("eventList", json);

        //salva filtri
        editor.putBoolean("party", isParty);
        editor.putBoolean("sagre", isSagre);
        editor.putBoolean("musica", isMusica);
        editor.putBoolean("sport", isSport);
        editor.putBoolean("altro", isAltro);

        editor.apply();
        super.onPause();
    }
    public void readDatabase(DatabaseReference db){
        // show loading
        progressBar.setVisibility(View.VISIBLE);

        db.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MyEvent event = dataSnapshot.getValue(MyEvent.class);
                    list.add(event);
                }
                myAdapter.setEventList(list);
                myAdapter.notifyDataSetChanged();

                filterEventsByDate(newFormattedDate);

                // hide loading
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    }

    @Override
    public void onEventRemoved(boolean success, int position) {

    }

    @Override
    public void onCheckboxSelected(boolean isParty, boolean isSagre, boolean isMusica, boolean isSport, boolean isAltro) {
        this.isParty = isParty;
        this.isSagre = isSagre;
        this.isMusica = isMusica;
        this.isSport = isSport;
        this.isAltro = isAltro;

        filterEventsByType();
    }
}