package com.example.zenaparty.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.zenaparty.R;
import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.MyEvent;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddEventFragment extends Fragment {
    private TextView dateTextView;
    private TextView timeTextView;

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_addevent, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        //create event listener for button to add event
        Button btnCreateEvent = view.findViewById(R.id.addEventButton);

        dateTextView = view.findViewById(R.id.tvSelectDate);
        timeTextView = view.findViewById(R.id.tvselectTime);
        // Imposta la data predefinita
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Mese è zero-based, quindi aggiungi 1
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String defaultDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month, year);
        dateTextView.setText(defaultDate);

// Imposta l'ora predefinita
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String defaultTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
        timeTextView.setText(defaultTime);
        dateTextView.setOnClickListener(v -> showDatePicker());
        timeTextView.setOnClickListener(v -> showTimePicker());


        btnCreateEvent.setOnClickListener(v -> {
            // hide keyboard
            InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            //get all the data from the form
            String eventName = ((EditText) view.findViewById(R.id.addEventName)).getText().toString();
            String eventDescription = ((EditText) view.findViewById(R.id.addEventDescription)).getText().toString();
            String eventLocation = ((EditText) view.findViewById(R.id.addEventAddress)).getText().toString();

            String eventDate = dateTextView.getText().toString();
            String eventTime = timeTextView.getText().toString();

            String eventType = ((Spinner) view.findViewById(R.id.addEventPlace)).getSelectedItem().toString();

            String eventHost = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            String eventPrice = ((EditText) view.findViewById(R.id.addEventPrice)).getText().toString();

            if (eventName.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventType.isEmpty() || Objects.requireNonNull(eventHost).isEmpty()) {
                Toast.makeText(getContext(), "One of the fields is empty", Toast.LENGTH_LONG).show();
                return;
            }

            MyEvent event = new MyEvent(eventName, eventDate, eventLocation, eventTime, eventType, eventPrice, eventDescription, eventHost);

            FirebaseWrapper.Database.saveEvent(event, getContext(), progressBar);
        });
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);
            dateTextView.setText(selectedDate);
        };

        // Impostazione della data attuale come predefinita nel DatePickerDialog
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timeTextView.setText(selectedTime);
        };

        // Impostazione dell'ora attuale come predefinita nel TimePickerDialog
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), timeSetListener, hour, minute, true);
        timePickerDialog.show();
    }
}