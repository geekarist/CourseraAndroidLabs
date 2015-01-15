package com.github.geekarist.dailyselfie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class NotifyService extends Service {
    private static final int NOTIFICATION_ID = 0;
    private static final String TAG = NotifyService.class.getSimpleName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service creating");
        HandlerThread thread = new HandlerThread("notifyServiceHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroying");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service starting command");
        Message msg = mServiceHandler.obtainMessage();
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message received");
            Intent notificationIntent = new Intent(NotifyService.this, ManageSelfiesActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(
                    NotifyService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(NotifyService.this)
                    .setContentIntent(contentIntent)
                    .setContentText("Yo")
                    .setContentTitle("Yeah")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        }
    }
}
