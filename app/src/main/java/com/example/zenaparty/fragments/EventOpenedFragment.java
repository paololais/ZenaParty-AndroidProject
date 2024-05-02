package com.example.zenaparty.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EventOpenedFragment extends Fragment {

    boolean isFavorite = false;
    String eventId;
    String title, eventDate, startTime, position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_opened, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get layout elements
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        TextView eventName = view.findViewById(R.id.eventName);
        TextView description = view.findViewById(R.id.eventDescription);
        TextView price = view.findViewById(R.id.eventPrice);
        TextView type = view.findViewById(R.id.eventType);
        TextView date = view.findViewById(R.id.eventDate);
        TextView time = view.findViewById(R.id.eventTime);
        TextView username = view.findViewById(R.id.userName);
        ImageView imgEvent = view.findViewById(R.id.imgEvent);
        TextView where = view.findViewById(R.id.eventLocation);

        ImageButton btnFavorite = view.findViewById(R.id.btnFavorite);
        ImageButton btnAddToCalendar = view.findViewById(R.id.btnAddToCalendar);
        ImageButton btnMaps = view.findViewById(R.id.btnMaps);

        // Recupera i dati dell'evento dall'argomento bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            MyEvent event = bundle.getParcelable("event");
            if (event != null) {
                eventName.setText(event.getEvent_name());
                description.setText(event.getDescription());
                price.setText(String.valueOf(event.getPrice()));
                type.setText(event.getType());
                date.setText(event.getDate());
                time.setText(event.getTime());
                where.setText(event.getLocation());

                FirebaseWrapper.Database.getUsername(event.getUsername(),username);

                eventId = String.valueOf(event.getEvent_id());
                startTime = event.getTime();
                title = event.getEvent_name();
                position = event.getLocation();
                eventDate = event.getDate();

                //set img based on event type
                setImage(imgEvent, event);
            }
        }

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("preferiti");
        
        //check se la key eventId esiste nel database e se è true/false
        //necessario per impostare elemento grafico
        setBtnFavoriteValue(btnFavorite, favoritesRef);

        btnClose.setOnClickListener(view1 -> requireActivity().onBackPressed());

        btnFavorite.setOnClickListener(view12 -> {
            if(!isFavorite){
                isFavorite = true;
                favoritesRef.child(eventId).setValue(true);
                btnFavorite.setImageResource(R.drawable.ic_favorite_true_foreground);

            } else {
                isFavorite = false;
                favoritesRef.child(eventId).setValue(false);
                btnFavorite.setImageResource(R.drawable.ic_favorite_foreground);
            }
        });

        btnAddToCalendar.setOnClickListener(view13 -> addEventToCalendar());
        btnMaps.setOnClickListener(view14 -> openLocationInMaps());
    }

    private void openLocationInMaps() {
        // Ottieni il nome del locale e la città
        String placeName = position;
        String city = "Genova";

        // Crea l'URI per la ricerca
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(placeName + ", " + city));

        // Crea l'Intent con l'azione ACTION_VIEW e l'URI
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Specifica che l'Intent deve essere gestito dall'app di Google Maps

        // Verifica se esiste un'app che può gestire l'Intent
        if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Avvia l'Intent per aprire l'app di Google Maps
            startActivity(mapIntent);
        } else {
            // Non è disponibile app di maps: messaggio di errore
            Toast.makeText(getContext(), "App di Maps non disponibile", Toast.LENGTH_SHORT).show();
        }
    }

    private void setBtnFavoriteValue(ImageButton btnFavorite, DatabaseReference favoritesRef) {
        favoritesRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))){
                    isFavorite = true;
                    btnFavorite.setImageResource(R.drawable.ic_favorite_true_foreground);
                } else {
                    isFavorite = false;
                    btnFavorite.setImageResource(R.drawable.ic_favorite_foreground);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setImage(ImageView imgEvent, MyEvent event) {
        switch (event.getType()) {
            case "Disco e Feste":
                imgEvent.setImageResource(R.drawable.party);
                break;
            case "Sagre":
                imgEvent.setImageResource(R.drawable.sagra);
                break;
            case "Musica":
                imgEvent.setImageResource(R.drawable.music1);
                break;
            case "Sport":
                imgEvent.setImageResource(R.drawable.sport);
                break;
            default:
                imgEvent.setImageResource(R.drawable.genoa);
                break;
        }
    }

    private void addEventToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, position);
        intent.putExtra(CalendarContract.Events.CALENDAR_ID, 1); // ID del calendario. Puoi ottenere l'ID del calendario desiderato.
        long startDateInMillis = getDateAndTimeInMillis();
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDateInMillis); // Imposta la data e ora di inizio dell'evento
        // Supponiamo di avere una durata dell'evento in minuti
        int eventDurationMinutes = 180;
        // Calcola la durata in millisecondi
        long eventDurationMillis = eventDurationMinutes * 60 * 1000;
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startDateInMillis + eventDurationMillis); // Imposta la data e ora di fine dell'evento (3 ore dopo)

        startActivity(intent);
    }

    long getDateAndTimeInMillis(){
        // Imposta il formato della data
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Date date = dateFormat.parse(eventDate);
            Date time = timeFormat.parse(startTime);

            // Crea un'istanza di Calendar e imposta l'ora
            Calendar calendar1 = Calendar.getInstance();
            assert date != null;
            calendar1.setTime(date);

            Calendar timeCalendar = Calendar.getInstance();
            assert time != null;
            timeCalendar.setTime(time);

            // Ottieni il timestamp in millisecondi
            calendar1.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar1.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

            // Imposta i secondi e i millisecondi a 0
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);

            // Ottieni il timestamp in millisecondi
            return calendar1.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  System.currentTimeMillis();
    }
}