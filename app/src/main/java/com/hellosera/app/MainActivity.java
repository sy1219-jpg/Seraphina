package com.hellosera.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private MediaPlayer player;
    private TextView status;
    private ImageView sera;

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

        TextView brand = tv("Hello, Sera", 17, ink, true);
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

        TextView sub = tv("버튼을 누르면 세라가 바로 말해요.", 15, muted, false);
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

        status = tv("눌러서 세라의 목소리를 들어보세요.", 13, muted, false);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(-1, -2);
        stp.topMargin = dp(12);
        root.addView(status, stp);

        btn.setOnClickListener(v -> playVoice());

        setContentView(scroll);
    }

    private void setSpeaking(boolean speaking) {
        if (sera == null) return;
        sera.animate()
                .scaleX(speaking ? 1.035f : 1f)
                .scaleY(speaking ? 1.035f : 1f)
                .setDuration(160)
                .start();
    }

    private void playVoice() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            player.release();
            player = null;
        }

        player = MediaPlayer.create(this, R.raw.sera_good_morning);
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
