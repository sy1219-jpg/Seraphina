package com.hellosera.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public final class SeraScheduler {
    public static final int SLOT_MORNING = 730;
    public static final int SLOT_LUNCH = 1200;
    public static final int SLOT_FOCUS = 1900;
    public static final int SLOT_NIGHT = 30;

    private static final int[][] SLOTS = new int[][] {
            {SLOT_MORNING, 7, 30},
            {SLOT_LUNCH, 12, 0},
            {SLOT_FOCUS, 19, 0},
            {SLOT_NIGHT, 0, 30}
    };

    private SeraScheduler() {}

    public static void scheduleAll(Context context) {
        for (int[] slot : SLOTS) {
            scheduleSlot(context, slot[0]);
        }
    }

    public static void scheduleSlot(Context context, int slotId) {
        int hour = hourFor(slotId);
        int minute = minuteFor(slotId);
        if (hour < 0) return;

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("slot", slotId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                slotId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar when = Calendar.getInstance();
        when.set(Calendar.HOUR_OF_DAY, hour);
        when.set(Calendar.MINUTE, minute);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);

        if (when.getTimeInMillis() <= System.currentTimeMillis()) {
            when.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAt = when.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static int latestSlotByClock() {
        Calendar now = Calendar.getInstance();
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        if (minutes >= 19 * 60) return SLOT_FOCUS;
        if (minutes >= 12 * 60) return SLOT_LUNCH;
        if (minutes >= 7 * 60 + 30) return SLOT_MORNING;
        if (minutes >= 30) return SLOT_NIGHT;

        // 00:00~00:29에는 전날 19:00 메시지가 가장 최근 메시지.
        return SLOT_FOCUS;
    }

    public static int hourFor(int slotId) {
        if (slotId == SLOT_MORNING) return 7;
        if (slotId == SLOT_LUNCH) return 12;
        if (slotId == SLOT_FOCUS) return 19;
        if (slotId == SLOT_NIGHT) return 0;
        return -1;
    }

    public static int minuteFor(int slotId) {
        if (slotId == SLOT_MORNING) return 30;
        if (slotId == SLOT_LUNCH) return 0;
        if (slotId == SLOT_FOCUS) return 0;
        if (slotId == SLOT_NIGHT) return 30;
        return -1;
    }
}
