package com.example.zenaparty.models;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.zenaparty.R;
import com.example.zenaparty.adapters.EventListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

// NOTE: With firebase we have to do a network request --> We need to add the permission in the AndroidManifest.xml
//      -> ref: https://developer.android.com/training/basics/network-ops/connecting

// Firebase auth - https://firebase.google.com/docs/auth/android/start?hl=en#java
// Firebase db - https://firebase.google.com/docs/database/android/start?hl=en

// 1) Create a new project from - https://firebase.google.com/ (console: https://console.firebase.google.com/u/0/)
// 2) Enable authentication: Build > Authentication > Get started , then enable Email/password (or other auth types)
// 3a) In Android Studio: Tools > Firebase > Authentication (or Realtime Database or the thing that you need!)
//      ( Then follow the instructions )
// 3b) Alternative you can connect firebase to your Android app - https://firebase.google.com/docs/android/setup?hl=en#register-app

public class FirebaseWrapper {
    public static class Callback {
        private final static String TAG = Callback.class.getCanonicalName();
        private final Method method;
        private final Object thiz;

        public Callback(Method method, Object thiz) {
            this.method = method;
            this.thiz = thiz;
        }

        public static Callback newInstance(Object thiz, String name, Class<?>... prms) {
            Class<?> clazz = thiz.getClass();
            try {
                return new Callback(clazz.getMethod(name, prms), thiz);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Cannot find method " + name + " in class " + clazz.getCanonicalName());

                // TODO: Better handling of the error
                throw new RuntimeException(e);
            }
        }

        public void invoke(Object... objs) {
            try {
                this.method.invoke(thiz, objs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.w(TAG, "Something went wrong during the callback. Message: " + e.getMessage());

                // TODO: Better handling of such an error
                throw new RuntimeException(e);
            }
        }
    }

    // Auth with email and password: https://firebase.google.com/docs/auth/android/password-auth?hl=en
    public static class Auth {
        private final static String TAG = Auth.class.getCanonicalName();
        private final FirebaseAuth auth;

        public Auth() {
            this.auth = FirebaseAuth.getInstance();
        }

        public boolean isAuthenticated() {
            return this.auth.getCurrentUser() != null;
        }

        public void signIn(String email, String password, ProgressBar progressBar, Callback callback) {
            progressBar.setVisibility(View.VISIBLE);
            this.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        callback.invoke(task.isSuccessful());
                        progressBar.setVisibility(View.GONE);
                    });
        }

        public void signUp(String email, String password, String username, ProgressBar progressBar, Callback callback) {
            progressBar.setVisibility(View.VISIBLE);
            this.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                String userEmail = user.getEmail();

                                // Salva email nel database "users"
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                usersRef.child(userId).child("email").setValue(userEmail);
                                usersRef.child(userId).child("username").setValue(username);

                                // Callback con esito positivo
                                callback.invoke(true);
                            } else {
                                // Gestione dell'errore
                                callback.invoke(false);
                            }
                        } else {
                            // Gestione dell'errore
                            callback.invoke(false);
                        }
                        progressBar.setVisibility(View.GONE);
                    });
        }

    }


    //database
    public static class Database {
        private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/").getReference("events");

        public Database() {

        }

        public static void saveEvent(MyEvent event, Context context, ProgressBar progressBar) {
            // Mostra il progresso di caricamento
            progressBar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.event_added)
                    .setPositiveButton("OK", (dialog, id) -> {
                    });
            AlertDialog dialog = builder.create();

            databaseReference.orderByChild("timestamp")
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Get the last event
                                DataSnapshot lastEventSnapshot = dataSnapshot.getChildren().iterator().next();
                                Long lastEventIdValue = lastEventSnapshot.child("event_id").getValue(Long.class);

                                // Step 2: Increment the retrieved "event_id" value
                                long newEventIdValue;
                                if (lastEventIdValue == null) {
                                    newEventIdValue = 0L;   // If the "event_id" value is null, set it to 1
                                } else {
                                    newEventIdValue = lastEventIdValue + 1;
                                }

                                // Step 3: Insert a new event with the incremented "event_id" value
                                event.setEvent_id(newEventIdValue);

                                // Push the new event to the events node
                                databaseReference.child(String.valueOf(newEventIdValue)).setValue(event);
                                //Toast.makeText(context, "Evento aggiunto", Toast.LENGTH_SHORT).show();
                                addToInsertedEvents(event);
                                Log.w("FirebaseWrapper", "New event inserted with ID: " + newEventIdValue);
                                progressBar.setVisibility(View.GONE);

                                dialog.show();
                            } else{
                                databaseReference.setValue(0);
                                Log.d("FirebaseWrapper", "Nodo 'events' creato con successo.");

                                // Step 3: Insert a new event with the incremented "event_id" value
                                long newEventIdValue = 0L;
                                event.setEvent_id(newEventIdValue);

                                // Push the new event to the events node
                                databaseReference.child(String.valueOf(newEventIdValue)).setValue(event);
                                //Toast.makeText(context, "Evento aggiunto", Toast.LENGTH_SHORT).show();
                                addToInsertedEvents(event);
                                Log.w("FirebaseWrapper", "New event inserted with ID: " + newEventIdValue);
                                progressBar.setVisibility(View.GONE);

                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("FirebaseWrapper", "Failed to read value.", error.toException());
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }

        public static void getCurrentUserFavorites(ArrayList<MyEvent> list, EventListAdapter myAdapter, ProgressBar progressBar, TextView tvNoEvents) {
            // Mostra il progresso di caricamento
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String currentUserId = auth.getCurrentUser().getUid();
                DatabaseReference usersReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("users");

                Query query = usersReference.child(currentUserId).child("preferiti").orderByKey();

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.getValue()==null) continue;
                            if (!(boolean)dataSnapshot.getValue()) continue;

                            // Ottieni l'ID dell'evento preferito dall'utente
                            String eventId = dataSnapshot.getKey();

                            // Cerca l'evento corrispondente nell'elenco degli eventi
                            DatabaseReference eventsReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("events");
                            assert eventId != null;
                            Query eventQuery = eventsReference.orderByChild("event_id").equalTo(Integer.parseInt(eventId));
                            eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot eventDataSnapshot : snapshot.getChildren()) {
                                        MyEvent event = eventDataSnapshot.getValue(MyEvent.class);
                                        list.add(event);
                                    }

                                    // Aggiorna l'adattatore e nascondi il progresso di caricamento
                                    myAdapter.setEventList(list);
                                    myAdapter.notifyDataSetChanged();

                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }

                        progressBar.setVisibility(View.GONE);
                        if (list.isEmpty()) {
                            tvNoEvents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoEvents.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }

        public static void getUserEventsInserted(ArrayList<MyEvent> list, EventListAdapter myAdapter, ProgressBar progressBar, TextView tvNoEvents) {
            // Mostra il progresso di caricamento
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String currentUserId = auth.getCurrentUser().getUid();
                DatabaseReference usersReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("users");

                Query query = usersReference.child(currentUserId).child("inserted_events").orderByKey();

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Ottieni l'ID dell'evento preferito dall'utente
                            String eventId = dataSnapshot.getKey();

                            // Cerca l'evento corrispondente nell'elenco degli eventi
                            DatabaseReference eventsReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("events");
                            assert eventId != null;
                            Query eventQuery = eventsReference.orderByChild("event_id").equalTo(Integer.parseInt(eventId));
                            eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot eventDataSnapshot : snapshot.getChildren()) {
                                        MyEvent event = eventDataSnapshot.getValue(MyEvent.class);
                                        list.add(event);
                                    }

                                    // Aggiorna l'adattatore e nascondi il progresso di caricamento
                                    myAdapter.setEventList(list);
                                    myAdapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            tvNoEvents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoEvents.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            tvNoEvents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoEvents.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }

        public static void addToInsertedEvents(MyEvent myEvent){

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();

                DatabaseReference insertedEventsRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("inserted_events");
                String eventId = String.valueOf(myEvent.getEvent_id());

                insertedEventsRef.child(eventId).setValue(true);
            }
        }

        public static void removeFromInsertedEvents(EventListInterface listInterface, MyEvent myEvent, int position){

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();

                DatabaseReference insertedEventsRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("inserted_events");
                String eventId = String.valueOf(myEvent.getEvent_id());

                insertedEventsRef.child(eventId).removeValue((error, ref) -> {
                    if (error ==null){
                        DatabaseReference eventsRef = FirebaseDatabase.getInstance()
                                .getReference("events")
                                .child(eventId);
                        eventsRef.removeValue((error1, ref1) -> {
                            if(error1 == null){
                                listInterface.onEventRemoved(true, position);
                                Log.d("firebase wrapper", "removed from inserted events");
                            }
                            else {
                                listInterface.onEventRemoved(false, position);
                                Log.d("firebase wrapper", "error removed from inserted events");
                            }
                        });
                    } else {
                        listInterface.onEventRemoved(false, position);
                        Log.d("firebase wrapper", " error removed from inserted events");
                    }
                });
            }
        }

        public static void modifyUsername(Context context, String newUsername, ProgressBar progressBar, TextView okUsername){
            // Mostra il progresso di caricamento
            progressBar.setVisibility(View.VISIBLE);
            okUsername.setVisibility(View.GONE);

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(userId).child("username").setValue(newUsername)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirebaseWrapper", "Modified username");
                            progressBar.setVisibility(View.GONE);
                            okUsername.setVisibility(View.VISIBLE);
                            Toast.makeText(context, "Username modificato", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseWrapper", "Failed to modify username: " + e.getMessage());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Errore durante la modifica dell'username", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // L'utente non è autenticato
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Utente non autenticato", Toast.LENGTH_SHORT).show();
            }
        }

        public static void getAndSetUsername(TextView usernameTv){
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.getValue(String.class);
                            // Imposta il valore dello username sul TextView
                            usernameTv.setText(username);
                        } else {
                            // Lo username non esiste nel database
                            Log.d("FirebaseWrapper", "Username non trovato nel database");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Gestisci eventuali errori di accesso al database
                        Log.e("FirebaseWrapper", "Errore durante il recupero dello username: " + databaseError.getMessage());
                    }
                });
            } else {
                // L'utente non è autenticato
                Log.d("FirebaseWrapper", "Utente non autenticato");
            }
        }

        public static void getUsername(String userId,TextView usernameTv){
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.getValue(String.class);
                        // Imposta il valore dello username sul TextView
                        usernameTv.setText(username);
                    } else {
                        // Lo username non esiste nel database
                        Log.d("FirebaseWrapper", "Username non trovato nel database");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Gestisci eventuali errori di accesso al database
                    Log.e("FirebaseWrapper", "Errore durante il recupero dello username: " + databaseError.getMessage());
                }
            });
        }

        public static void getHostInsertedEvents(String userId, ArrayList<MyEvent> list, EventListAdapter myAdapter, ProgressBar progressBar, TextView tvNoEvents) {
            if (userId !=null) {
                DatabaseReference usersReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("users");

                Query query = usersReference.child(userId).child("inserted_events").orderByKey();

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String eventId = dataSnapshot.getKey();

                            // Cerca l'evento corrispondente nell'elenco degli eventi
                            DatabaseReference eventsReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("events");
                            assert eventId != null;
                            Query eventQuery = eventsReference.orderByChild("event_id").equalTo(Integer.parseInt(eventId));
                            eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot eventDataSnapshot : snapshot.getChildren()) {
                                        MyEvent event = eventDataSnapshot.getValue(MyEvent.class);
                                        list.add(event);
                                    }

                                    // Aggiorna l'adattatore e nascondi il progresso di caricamento
                                    myAdapter.setEventList(list);
                                    myAdapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.GONE);

                                    if (list.isEmpty()) {
                                        tvNoEvents.setVisibility(View.VISIBLE);
                                    } else {
                                        tvNoEvents.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            tvNoEvents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoEvents.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            tvNoEvents.setVisibility(View.VISIBLE);
                        } else {
                            tvNoEvents.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                tvNoEvents.setVisibility(View.VISIBLE);
            }
        }

        // Metodo per ottenere la valutazione media dell'utente host
        public static void getHostRating(String hostUserId, RatingBar ratingBar, TextView ratingValueTV, TextView numberOfReviewsTV) {
            DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("users").child(hostUserId).child("ratings");
            ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    float totalRating = 0;
                    int ratingCount = 0;

                    // Itera su tutti i nodi dei rating
                    for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                        // Ottieni il valore del rating e aggiungilo al totale
                        Float rating = ratingSnapshot.getValue(Float.class);
                        if (rating != null) {
                            // Add the rating to the total and increment the count
                            totalRating += rating;
                            ratingCount++;
                        }
                    }
                    String ratingCountText = "(" + ratingCount + ")";
                    numberOfReviewsTV.setText(ratingCountText);
                    // Calcola la media dei rating
                    float averageRating = (ratingCount > 0) ? totalRating / ratingCount : 0;
                    ratingBar.setRating(averageRating);

                    String formattedRating = String.format(Locale.getDefault(), "%.1f", averageRating);
                    ratingValueTV.setText(formattedRating);

                    Log.d("FirebaseWrapper", "Average Rating: " + averageRating);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Gestisci eventuali errori durante il recupero dei dati
                    Log.e("FirebaseWrapper", "Error retrieving ratings: " + error.getMessage());
                }
            });
        }

        public static void sendNewRating(String userId, float newRating){
            if(FirebaseAuth.getInstance().getCurrentUser()!= null){
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // Ottieni il riferimento al nodo "ratings" dell'utente nel database Firebase
                DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("ratings");

                // Aggiorna il nodo "ratings" con il nuovo rating
                ratingsRef.child(currentUserId).setValue(newRating)
                        .addOnSuccessListener(aVoid -> {
                            // Gestisci il successo dell'aggiornamento del rating
                            Log.d("FirebaseWrapper", "New rating sent successfully");
                        })
                        .addOnFailureListener(e -> {
                            // Gestisci eventuali errori durante l'aggiornamento del rating
                            Log.e("FirebaseWrapper", "Error sending new rating: " + e.getMessage());
                        });
            }
        }
    }
}
