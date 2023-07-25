package com.example.zenaparty.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyNotificationWorker extends Worker {

    public MyNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        boolean controlFlag = this.getApplicationContext().getSharedPreferences("SavedValues", Context.MODE_PRIVATE).getBoolean("Notifiche", true);

        if (!controlFlag) {
            Log.d("MyNotificationWorker", "Notifica Non Inviata");
            return Result.success();
        }

        //test log
        System.out.println("MyNotificationWorker.startWork()");
        Log.d("MyNotificationWorker", "startWork()");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "zena_party")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Zena Party")
                .setContentText("Controlla i nuovi eventi disponibili per oggi")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());


        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();
        }

        notificationManager.notify(200, builder.build());

        return Result.success();
    }
}
