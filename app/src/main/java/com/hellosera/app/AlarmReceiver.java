package com.hellosera.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "sera_letters";

    @Override
    public void onReceive(Context context, Intent intent) {
        int slot = intent.getIntExtra("slot", SeraScheduler.latestSlotByClock());

        context.getSharedPreferences("sera_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("latest_slot", slot)
                .apply();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "세라의 편지",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("정해진 시간에 세라가 보내는 한마디");
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent open = new Intent(context, MainActivity.class);
        open.putExtra("slot", slot);
        open.putExtra("autoplay", true);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                slot,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        builder.setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("💌 세라에게서 편지가 왔어요!")
                .setContentText("눌러서 세라의 목소리를 들어보세요.")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        manager.notify(slot, builder.build());

        // 다음 날 같은 시간의 알림을 다시 예약.
        SeraScheduler.scheduleSlot(context, slot);
    }
}
