package com.example.zenaparty.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.zenaparty.models.FirebaseWrapper;
import com.example.zenaparty.models.PermissionManager;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // NOTE: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        // Restart the worker
        if (new FirebaseWrapper.Auth().isAuthenticated()) {

         for (String permission : PermissionManager.NEEDED_PERMISSIONS) {
              if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                  Log.d(TAG, "The BR can not execute. " + permission + " is needed.");
                  return;
              }
         }

            OneTimeWorkRequest worker = new OneTimeWorkRequest.Builder(MyNotificationWorker.class).build();
            WorkManager.getInstance(context).enqueue(worker);
        }

    }
}