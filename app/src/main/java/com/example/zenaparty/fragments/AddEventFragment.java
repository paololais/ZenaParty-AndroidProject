package com.example.zenaparty.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.Objects;

public class AddEventFragment extends Fragment {

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_addevent, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        //create event listener for button to add event
        Button btnCreateEvent = view.findViewById(R.id.addEventButton);

        btnCreateEvent.setOnClickListener(v -> {
            //get all the data from the form
            String eventName = ((EditText)view.findViewById(R.id.addEventName)).getText().toString();
            String eventDescription = ((EditText)view.findViewById(R.id.addEventDescription)).getText().toString();
            String eventLocation = ((EditText)view.findViewById(R.id.addEventAddress)).getText().toString();

            DatePicker datePicker = view.findViewById(R.id.datePicker);
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;  // Months in DatePicker are zero-based, so add 1
            int year = datePicker.getYear();


            Log.d("AddEventFragment", "Date: " + day + "-" + month + "-" + year);

            //use Locale to get date in the format dd-mm-yyyy
            String eventDate = String.format(Locale.getDefault(),"%02d-%02d-%04d", day, month, year);
            //get date from date picker

            Log.d("AddEventFragment", "Date: " + eventDate);

            TimePicker timePicker = view.findViewById(R.id.timePicker);
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Log.d("AddEventFragment", "Time: " + hour + ":" + minute);

            String eventTime = String.format(Locale.getDefault(),"%02d:%02d", hour, minute);


            Log.d("AddEventFragment", "Time: " + eventTime);

            Spinner addEventPlaceSpinner = view.findViewById(R.id.addEventPlace);
            String eventType = addEventPlaceSpinner.getSelectedItem().toString();



            String eventHost = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
            String eventPrice = ((EditText)view.findViewById(R.id.addEventPrice)).getText().toString();


            Log.d("AddEventFragment", eventHost);

            //if any of the fields are empty, return
            if(eventName.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventType.isEmpty() || Objects.requireNonNull(eventHost).isEmpty()) {
                //display error message
                Log.d("AddEventFragment", "One of the fields is empty");

                return;
            }

            Log.d("AddEventFragment", "Attempting to create event object");

            //create event object
            MyEvent event = new MyEvent(eventName, eventDate,  eventLocation, eventTime, eventType, eventPrice, eventDescription, eventHost);

            //push with firebase
            //debug
            Log.d("AddEventFragment", "Attempting to add event to database");

            //push to database
            FirebaseWrapper.Database.saveEvent(event, getContext(), progressBar);
        });


    }

}