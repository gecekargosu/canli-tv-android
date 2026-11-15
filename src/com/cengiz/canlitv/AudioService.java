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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Ekran kapaliyken / kilitliyken sesli devam eden on plan servisi.
 * PARTIAL_WAKE_LOCK + WIFI lock sayesinde kilit ekraninda akis kesilmez.
 */
public class AudioService extends Service {

    public static final String CHANNEL_ID = "umay_audio";
    private static final int NOTIF_ID = 7;

    private MediaPlayer player;
    private String name = "Umay Multi TV";
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

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
        if (intent.getBooleanExtra("stop", false)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String url = intent.getStringExtra("url");
        name = intent.getStringExtra("name") == null ? "Umay Multi TV" : intent.getStringExtra("name");
        createChannel();
        startForeground(NOTIF_ID, notification());
        locks();
        if (url != null) {
            releasePlayer();
            try {
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
                player.setDataSource(url);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        try { startForeground(NOTIF_ID, notification()); } catch (Exception ignored) { }
                    }
                });
                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override public boolean onError(MediaPlayer mp, int w, int e) {
                        try {
                            NotificationManager nm =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                                    ? new Notification.Builder(AudioService.this, CHANNEL_ID)
                                    : new Notification.Builder(AudioService.this);
                            nm.notify(NOTIF_ID, b.setContentTitle(name)
                                    .setContentText("Yayın açılamadı")
                                    .setSmallIcon(android.R.drawable.ic_media_play).build());
                        } catch (Exception ignored) { }
                        return true;
                    }
                });
            } catch (Exception e) {
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void locks() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "umay:audio");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
            if (wifiLock == null) {
                WifiManager wm = (WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "umay:wifi");
                    wifiLock.setReferenceCounted(false);
                    wifiLock.acquire();
                }
            }
        } catch (Exception ignored) { }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Umay ses yayını",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification notification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stop = new Intent(this, AudioService.class);
        stop.putExtra("stop", true);
        PendingIntent stopPi = PendingIntent.getService(this, 1, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        b.setContentTitle(name)
                .setContentText("Ekran kapalıyken de çalıyor · dinlemeye devam")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setDeleteIntent(stopPi)
                .setOngoing(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        return b.build();
    }

    private void releasePlayer() {
        try {
            if (player != null) { player.reset(); player.release(); player = null; }
        } catch (Exception ignored) { }
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        try { if (wakeLock != null) { wakeLock.release(); wakeLock = null; } } catch (Exception ignored) { }
        try { if (wifiLock != null) { wifiLock.release(); wifiLock = null; } } catch (Exception ignored) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
