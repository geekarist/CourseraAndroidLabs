package com.github.geekarist.dailyselfie;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// TODO: use a Service which will stay alive, because an IntentService won't
// See http://developer.android.com/guide/components/services.html#ExtendingService
public class NotifyService extends IntentService {
    private static final int NOTIFICATION_ID = 0;
    private static final String TAG = NotifyService.class.getSimpleName();

    public NotifyService() {
        super("NotifyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent received");
        Intent notificationIntent = new Intent(this, ManageSelfiesActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
