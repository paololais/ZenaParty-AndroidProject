package com.example.zenaparty.models;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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

    public interface OnReviewCheckListener {
        void onReviewChecked(boolean userReviewed);
    }

    public interface OnEventSavedListener {
        void onEventSavedSuccessfully(boolean success);
    }

    public interface OnQRCodeFoundListener {
        void onQRCodeFound(boolean found);
    }

    //database
    public static class Database {
        private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/").getReference("events");

        public Database() {

        }

        public static void saveEvent(MyEvent event, String eventId,Context context, ProgressBar progressBar, OnEventSavedListener eventSavedListener) {
            // Mostra il progresso di caricamento
            progressBar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.event_added)
                    .setPositiveButton("OK", (dialog, id) -> {
                    });
            AlertDialog dialog = builder.create();

            // Salvo l'evento nel database Firebase sotto la chiave generata
            databaseReference.child(eventId).setValue(event)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.w("FirebaseWrapper", "New event inserted with ID: " + eventId);
                            progressBar.setVisibility(View.GONE);
                            eventSavedListener.onEventSavedSuccessfully(true);
                            dialog.show();
                        } else {
                            Log.e("FirebaseWrapper", "Error inserting new event: " + task.getException());
                            progressBar.setVisibility(View.GONE);
                            eventSavedListener.onEventSavedSuccessfully(false);
                            Toast.makeText(context, "Error inserting new event", Toast.LENGTH_SHORT).show();
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
                            if (dataSnapshot.getValue() == null) continue;
                            if (!(boolean) dataSnapshot.getValue()) continue;

                            // Ottieni l'ID dell'evento preferito dall'utente
                            String eventId = dataSnapshot.getKey();

                            // Cerca l'evento corrispondente nell'elenco degli eventi
                            DatabaseReference eventsReference = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("events");
                            assert eventId != null;
                            Query eventQuery = eventsReference.orderByChild("event_id").equalTo(eventId);
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
        public static void removeFromUserFavorites(String eventId){
            DatabaseReference usersRef = FirebaseDatabase.getInstance()
                    .getReference("users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        if (userId != null) {
                            // Verifica se l'evento è nei preferiti di questo utente
                            DatabaseReference userFavoritesRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(userId)
                                    .child("preferiti")
                                    .child(eventId);
                            userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Se l'evento è nei preferiti di questo utente, rimuovilo
                                        userFavoritesRef.removeValue();
                                        Log.d("firebase wrapper", "removed from user favorites: " + userId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Gestisci eventuali errori di lettura del database
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        public static void removeFromInsertedEvents(EventListInterface listInterface, Context context, MyEvent myEvent, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.conferma_rimozione)
                    .setMessage(R.string.rimozione_evento)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        String eventId = String.valueOf(myEvent.getEvent_id());
                        DatabaseReference eventsRef = FirebaseDatabase.getInstance()
                                .getReference("events")
                                .child(eventId);
                        eventsRef.removeValue((error1, ref1) -> {
                            if (error1 == null) {
                                listInterface.onEventRemoved(true, position);
                                removeFromUserFavorites(eventId);
                                Log.d("firebase wrapper", "removed from inserted events");
                            } else {
                                listInterface.onEventRemoved(false, position);
                                Log.d("firebase wrapper", "error while removing from inserted events");
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        public static void modifyUsername(Context context, String newUsername, ProgressBar progressBar, TextView okUsername) {
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

        public static void getAndSetUsername(TextView usernameTv) {
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

        public static void getUsername(String userId, TextView usernameTv) {
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

        public static void getUserInsertedEvents(String userId, ArrayList<MyEvent> list, EventListAdapter myAdapter, ProgressBar progressBar, TextView tvNoEvents) {
            if (userId != null) {
                DatabaseReference eventsRef = FirebaseDatabase.getInstance("https://pmappfirsttry-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("events");
                eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            MyEvent event = eventSnapshot.getValue(MyEvent.class);
                            if (event != null && event.getUserId().equals(userId)) {
                                list.add(event);
                            }
                        }
                        myAdapter.notifyDataSetChanged();
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

        public static void sendNewRating(String userId, RatingBar ratingBar, TextView ratingValueTV, TextView numberOfReviewsTV, ImageButton deleteReviewBtn) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                float newRating = ratingBar.getRating();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("ratings");

                ratingsRef.child(currentUserId).setValue(newRating)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirebaseWrapper", "New rating sent successfully");
                            getHostRating(userId, ratingBar, ratingValueTV, numberOfReviewsTV);
                            deleteReviewBtn.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> Log.e("FirebaseWrapper", "Error sending new rating: " + e.getMessage()));
            }
        }

        public static void removeReview(String hostUserId, RatingBar ratingBar, TextView ratingValueTV, TextView numberOfReviewsTV, ImageButton deleteReviewBtn) {
            // Se l'utente conferma la rimozione, procedi con la logica di rimozione
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // Ottieni l'ID dell'utente corrente
                String currentUserId = currentUser.getUid();
                DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(hostUserId).child("ratings").child(currentUserId);

                ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // La recensione esiste, rimuovila
                            ratingsRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirebaseWrapper", "Review removed successfully");
                                        getHostRating(hostUserId,ratingBar,ratingValueTV,numberOfReviewsTV);
                                        deleteReviewBtn.setVisibility(View.GONE);
                                    })
                                    .addOnFailureListener(e -> Log.e("FirebaseWrapper", "Error removing review: " + e.getMessage()));
                        } else {
                            // Non esiste una recensione dell'utente corrente per l'host
                            Log.d("FirebaseWrapper", "No review found for current user");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Gestisci eventuali errori durante il recupero dei dati
                        Log.e("FirebaseWrapper", "Error retrieving review: " + error.getMessage());
                    }
                });
            } else {
                // L'utente corrente non è autenticato
                Log.d("FirebaseWrapper", "User not authenticated");
            }

        }
        public static void checkIfUserReviewedHost(String hostUserId, String currentUserUid, OnReviewCheckListener listener) {
            DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("users").child(hostUserId).child("ratings");

            // Controlla se l'utente corrente ha inserito una recensione per l'host selezionato
            ratingsRef.child(currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Se l'utente ha inserito una recensione, restituisci true al listener
                    // Altrimenti, restituisci false al listener
                    listener.onReviewChecked(snapshot.exists());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Gestisci eventuali errori durante il recupero dei dati
                    Log.e("FirebaseWrapper", "Error checking review: " + error.getMessage());
                }
            });
        }

        //method to check if a QR code exists in the DB
        public static void checkQRCodeValidity(String QRCodeID, OnQRCodeFoundListener listener){
            DatabaseReference QRCodesRef = FirebaseDatabase.getInstance().getReference("qr_codes");

            QRCodesRef.child(QRCodeID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listener.onQRCodeFound(snapshot.exists());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Gestisci eventuali errori durante il recupero dei dati
                    Log.e("FirebaseWrapper", "Error checking QR Code Validity: " + error.getMessage());
                }
            });
        }

        public static void CreateQRCode(String QRMessage){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // Ottieni l'ID dell'utente corrente
                String currentUserId = currentUser.getUid();
                DatabaseReference QRCodesRef = FirebaseDatabase.getInstance().getReference("qr_codes");
                QRCodesRef.child(currentUserId).setValue(QRMessage)
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseWrapper", "New QR Code saved successfully"))
                        .addOnFailureListener(e -> Log.e("FirebaseWrapper", "Error creating new QR Code: " + e.getMessage()));
            }
        }

        public static void ReadQRCode(String qrCodeID, TextView messageTV){
            DatabaseReference QRCodeRef = FirebaseDatabase.getInstance().getReference("qr_codes").child(qrCodeID);

            QRCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String message = dataSnapshot.getValue(String.class);
                        messageTV.setText(message);
                    } else {
                        // Lo username non esiste nel database
                        Log.d("FirebaseWrapper", "QR Code non trovato nel database");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Gestisci eventuali errori di accesso al database
                    Log.e("FirebaseWrapper", "Errore durante il recupero del QR Code: " + databaseError.getMessage());
                }
            });
        }

        public static void DeleteQRCode(String qrCodeID){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // Ottieni l'ID dell'utente corrente
                String currentUserId = currentUser.getUid();
                DatabaseReference QRCodeRef = FirebaseDatabase.getInstance().getReference("qr_codes").child(qrCodeID);

                QRCodeRef.removeValue()
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseWrapper", "QR Code ddeleted successfully"))
                        .addOnFailureListener(e-> Log.e("FirebaseWrapper", "Error while deleting QR code"));
            }
        }

        public static void GetUserQrCodes(){

        }


    }
}
