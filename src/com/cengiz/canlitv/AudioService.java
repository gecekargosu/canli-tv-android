package com.cengiz.canlitv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

/** Ekran kapaliyken / kilitliyken sesli devam eden on plan servisi. */
public class AudioService extends Service {

    public static final String CHANNEL_ID = "canlitv_audio";
    private MediaPlayer player;
    private String name = "Canlı TV";

    public static void start(Context ctx, String url, String channelName) {
        Intent i = new Intent(ctx, AudioService.class);
        i.putExtra("url", url);
        i.putExtra("name", channelName);
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    public static void stop(Context ctx) {
        ctx.stopService(new Intent(ctx, AudioService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String url = intent.getStringExtra("url");
        name = intent.getStringExtra("name") == null ? "Canlı TV" : intent.getStringExtra("name");
        createChannel();
        startForeground(7, notification());
        if (url != null && player == null) {
            try {
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build());
                player.setDataSource(url);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) { mp.start(); }
                });
            } catch (Exception e) {
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Canlı TV ses",
                    NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification notification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        b.setContentTitle(name)
                .setContentText("Arka planda çalıyor · dokun ve geri dön")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setOngoing(true);
        return b.build();
    }

    @Override
    public void onDestroy() {
        try {
            if (player != null) { player.stop(); player.release(); player = null; }
        } catch (Exception ignored) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
