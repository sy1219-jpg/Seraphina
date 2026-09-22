package com.hellosera.app;

import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private MediaPlayer player;
    private TextView status;
    private SeraView sera;

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private TextView tv(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        int bg=Color.rgb(251,247,239), ink=Color.rgb(45,43,40), muted=Color.rgb(122,117,109), accent=Color.rgb(111,128,105);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22),dp(22),dp(22),dp(28)); root.setBackgroundColor(bg);
        TextView brand=tv("Hello, Sera",17,ink,true); brand.setGravity(Gravity.START); root.addView(brand,new LinearLayout.LayoutParams(-1,-2));
        sera=new SeraView(this); LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(dp(270),dp(290)); slp.topMargin=dp(8); root.addView(sera,slp);
        TextView title=tv("세라에게서\n편지가 도착했어요.",29,ink,true); title.setLineSpacing(0,1.12f); root.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=tv("버튼을 누르면 세라가 바로 말해요.",15,muted,false); LinearLayout.LayoutParams sup=new LinearLayout.LayoutParams(-1,-2); sup.topMargin=dp(8); root.addView(sub,sup);

        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER); card.setPadding(dp(18),dp(18),dp(18),dp(18));
        GradientDrawable cb=new GradientDrawable(); cb.setColor(Color.rgb(255,253,250)); cb.setCornerRadius(dp(24)); cb.setStroke(dp(1),Color.rgb(232,223,210)); card.setBackground(cb);
        card.addView(tv("💌",42,ink,false)); card.addView(tv("오늘의 한마디가 도착했어요.",21,ink,true));
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,-2); clp.topMargin=dp(16); root.addView(card,clp);

        Button btn=new Button(this); btn.setText("편지가 왔어요!"); btn.setAllCaps(false); btn.setTextSize(20); btn.setTextColor(Color.WHITE); btn.setTypeface(Typeface.DEFAULT,Typeface.BOLD); btn.setStateListAnimator(null);
        GradientDrawable bb=new GradientDrawable(); bb.setColor(accent); bb.setCornerRadius(dp(20)); btn.setBackground(bb);
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,dp(62)); blp.topMargin=dp(18); root.addView(btn,blp);
        status=tv("눌러서 세라의 목소리를 들어보세요.",13,muted,false); LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,-2); stp.topMargin=dp(12); root.addView(status,stp);
        btn.setOnClickListener(v -> playVoice());
        setContentView(root);
    }

    private void playVoice() {
        if (player != null) { try { player.stop(); } catch(Exception ignored) {} player.release(); }
        player = MediaPlayer.create(this, R.raw.sera_good_morning);
        if (player == null) { status.setText("음성을 재생할 수 없어요."); return; }
        status.setText("세라가 말하고 있어요… 🔊"); sera.setSpeaking(true);
        player.setOnCompletionListener(mp -> { sera.setSpeaking(false); status.setText("다 들었어요. 다시 눌러도 돼요."); mp.release(); if(player==mp) player=null; });
        player.start();
    }

    @Override protected void onDestroy() { if(player!=null){player.release();player=null;} super.onDestroy(); }

    public static class SeraView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); boolean speaking=false;
        public SeraView(android.content.Context c) { super(c); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        void setSpeaking(boolean b) { speaking=b; animate().scaleX(b?1.035f:1f).scaleY(b?1.035f:1f).setDuration(160).start(); invalidate(); }
        protected void onDraw(Canvas c) { super.onDraw(c); float w=getWidth(), h=getHeight(); c.drawColor(Color.TRANSPARENT);
            p.setColor(0x22000000); c.drawOval(w*.25f,h*.87f,w*.75f,h*.94f,p);
            p.setColor(0xFFFFD9B8); c.drawRoundRect(w*.42f,h*.70f,w*.49f,h*.88f,20,20,p); c.drawRoundRect(w*.51f,h*.70f,w*.58f,h*.88f,20,20,p);
            p.setColor(0xFF875338); c.drawOval(w*.38f,h*.84f,w*.49f,h*.91f,p); c.drawOval(w*.51f,h*.84f,w*.62f,h*.91f,p);
            p.setColor(0xFF304D83); c.drawRoundRect(w*.34f,h*.49f,w*.66f,h*.76f,34,34,p); c.drawRect(w*.37f,h*.68f,w*.63f,h*.79f,p);
            p.setColor(0xFF75462F); c.drawOval(w*.27f,h*.13f,w*.73f,h*.52f,p);
            p.setColor(0xFFFFD9B8); c.drawOval(w*.32f,h*.20f,w*.68f,h*.51f,p);
            p.setColor(0xFF75462F); c.drawOval(w*.27f,h*.26f,w*.38f,h*.55f,p); c.drawOval(w*.62f,h*.26f,w*.73f,h*.55f,p);
            Path bang=new Path(); bang.moveTo(w*.33f,h*.26f); bang.quadTo(w*.45f,h*.10f,w*.57f,h*.23f); bang.quadTo(w*.62f,h*.16f,w*.68f,h*.30f); bang.lineTo(w*.67f,h*.18f); bang.lineTo(w*.34f,h*.16f); bang.close(); p.setColor(0xFF75462F); c.drawPath(bang,p);
            p.setColor(0xFF241B18); p.setStrokeWidth(Math.max(4,w*.012f)); p.setStyle(Paint.Style.STROKE); c.drawArc(w*.39f,h*.32f,w*.46f,h*.38f,10,160,false,p); c.drawArc(w*.54f,h*.32f,w*.61f,h*.38f,10,160,false,p); c.drawArc(w*.43f,h*.37f,w*.57f,h*.47f,10,160,false,p); p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFF9C92); c.drawCircle(w*.38f,h*.41f,w*.035f,p); c.drawCircle(w*.62f,h*.41f,w*.035f,p);
            p.setColor(0xFFFFD62E); c.drawCircle(w*.31f,h*.39f,w*.023f,p); c.drawCircle(w*.69f,h*.39f,w*.023f,p);
            p.setColor(Color.WHITE); Path blouse=new Path(); blouse.moveTo(w*.45f,h*.51f); blouse.lineTo(w*.55f,h*.51f); blouse.lineTo(w*.50f,h*.62f); blouse.close(); c.drawPath(blouse,p);
            p.setColor(0xFF304D83); p.setStrokeWidth(w*.09f); p.setStrokeCap(Paint.Cap.ROUND); c.drawLine(w*.39f,h*.57f,w*.49f,h*.70f,p); c.drawLine(w*.61f,h*.57f,w*.51f,h*.70f,p);
            p.setColor(0xFFFFD9B8); c.drawCircle(w*.48f,h*.70f,w*.035f,p); c.drawCircle(w*.52f,h*.70f,w*.035f,p);
            if(speaking) { p.setColor(0xFF6F8069); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(w*.012f); c.drawArc(w*.76f,h*.29f,w*.88f,h*.43f,-60,120,false,p); c.drawArc(w*.79f,h*.26f,w*.94f,h*.46f,-60,120,false,p); p.setStyle(Paint.Style.FILL); }
        }
    }
}
