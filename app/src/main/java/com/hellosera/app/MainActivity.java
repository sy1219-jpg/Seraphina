package com.hellosera.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;

    private MediaPlayer player;
    private TextView status;
    private ImageView sera;
    private Intent lastHandledIntent;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + .5f);
    }

    private TextView tv(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        int bg = Color.rgb(251, 247, 239);
        int ink = Color.rgb(45, 43, 40);
        int muted = Color.rgb(122, 117, 109);
        int accent = Color.rgb(111, 128, 105);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(22), dp(22), dp(28));
        root.setBackgroundColor(bg);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -1));

        TextView brand = tv("재원이에게", 17, ink, true);
        brand.setGravity(Gravity.START);
        root.addView(brand, new LinearLayout.LayoutParams(-1, -2));

        sera = new ImageView(this);
        sera.setImageResource(R.drawable.sera_character);
        sera.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sera.setAdjustViewBounds(true);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(270), dp(290));
        slp.topMargin = dp(8);
        root.addView(sera, slp);

        TextView title = tv("세라에게서\n편지가 도착했어요.", 29, ink, true);
        title.setLineSpacing(0, 1.12f);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView sub = tv("알림을 누르거나 아래 버튼을 누르면 세라가 바로 말해요.", 15, muted, false);
        LinearLayout.LayoutParams sup = new LinearLayout.LayoutParams(-1, -2);
        sup.topMargin = dp(8);
        root.addView(sub, sup);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        GradientDrawable cb = new GradientDrawable();
        cb.setColor(Color.rgb(255, 253, 250));
        cb.setCornerRadius(dp(24));
        cb.setStroke(dp(1), Color.rgb(232, 223, 210));
        card.setBackground(cb);

        card.addView(tv("💌", 42, ink, false));
        card.addView(tv("오늘의 한마디가 도착했어요.", 21, ink, true));

        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.topMargin = dp(16);
        root.addView(card, clp);

        Button btn = new Button(this);
        btn.setText("편지가 왔어요!");
        btn.setAllCaps(false);
        btn.setTextSize(20);
        btn.setTextColor(Color.WHITE);
        btn.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btn.setStateListAnimator(null);

        GradientDrawable bb = new GradientDrawable();
        bb.setColor(accent);
        bb.setCornerRadius(dp(20));
        btn.setBackground(bb);

        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, dp(62));
        blp.topMargin = dp(18);
        root.addView(btn, blp);

        status = tv("가장 최근에 도착한 세라의 편지를 다시 들을 수 있어요.", 13, muted, false);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(-1, -2);
        stp.topMargin = dp(12);
        root.addView(status, stp);

        btn.setOnClickListener(v -> playLatestVoice());

        setContentView(scroll);

        requestNotificationPermissionIfNeeded();
        SeraScheduler.scheduleAll(this);
        maybeRequestExactAlarmPermission();
        handleLaunchIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 정확한 알람 권한을 방금 허용하고 돌아온 경우에도 시간을 다시 등록.
        SeraScheduler.scheduleAll(this);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST
            );
        }
    }

    private void maybeRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null || alarmManager.canScheduleExactAlarms()) return;

        boolean prompted = getSharedPreferences("sera_prefs", MODE_PRIVATE)
                .getBoolean("exact_alarm_prompted", false);
        if (prompted) return;

        getSharedPreferences("sera_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("exact_alarm_prompted", true)
                .apply();

        new AlertDialog.Builder(this)
                .setTitle("세라의 편지를 시간 맞춰 받을까요?")
                .setMessage("07:30, 12:00, 19:00, 00:30에 정확히 알림을 받으려면 ‘알람 및 리마인더’ 권한을 허용해 주세요.")
                .setPositiveButton("허용하기", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + getPackageName())
                        );
                        startActivity(intent);
                    } catch (Exception ignored) {
                        Toast.makeText(
                                this,
                                "설정에서 Hello, Sera의 ‘알람 및 리마인더’를 허용해 주세요.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .setNegativeButton("나중에", null)
                .show();
    }

    private void handleLaunchIntent(Intent intent) {
        if (intent == null || intent == lastHandledIntent) return;
        lastHandledIntent = intent;

        boolean autoplay = intent.getBooleanExtra("autoplay", false);
        int slot = intent.getIntExtra("slot", -1);

        if (autoplay && slot != -1) {
            status.postDelayed(() -> playSlot(slot), 300);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        lastHandledIntent = null;
        handleLaunchIntent(intent);
    }

    private void playLatestVoice() {
        int latest = getSharedPreferences("sera_prefs", MODE_PRIVATE)
                .getInt("latest_slot", -1);
        if (latest == -1) {
            latest = SeraScheduler.latestSlotByClock();
        }
        playSlot(latest);
    }

    private int rawResourceFor(int slot) {
        if (slot == SeraScheduler.SLOT_MORNING) return R.raw.voice_0730;
        if (slot == SeraScheduler.SLOT_LUNCH) return R.raw.voice_1200;
        if (slot == SeraScheduler.SLOT_FOCUS) return R.raw.voice_1900;
        if (slot == SeraScheduler.SLOT_NIGHT) return R.raw.voice_0030;
        return R.raw.voice_0730;
    }

    private void setSpeaking(boolean speaking) {
        if (sera == null) return;
        sera.animate()
                .scaleX(speaking ? 1.035f : 1f)
                .scaleY(speaking ? 1.035f : 1f)
                .setDuration(160)
                .start();
    }

    private void playSlot(int slot) {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            player.release();
            player = null;
        }

        player = MediaPlayer.create(this, rawResourceFor(slot));
        if (player == null) {
            status.setText("음성을 재생할 수 없어요.");
            return;
        }

        status.setText("세라가 말하고 있어요… 🔊");
        setSpeaking(true);

        player.setOnCompletionListener(mp -> {
            setSpeaking(false);
            status.setText("다 들었어요. 다시 눌러도 돼요.");
            mp.release();
            if (player == mp) player = null;
        });

        player.setOnErrorListener((mp, what, extra) -> {
            setSpeaking(false);
            status.setText("음성을 재생할 수 없어요.");
            try { mp.release(); } catch (Exception ignored) {}
            if (player == mp) player = null;
            return true;
        });

        player.start();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
