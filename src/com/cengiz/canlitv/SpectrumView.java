package com.cengiz.canlitv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * Gerçek zamanlı ses tayfı (v10.02).
 *
 * Değerler AudioService'ten gelir: radyonun kendi çözdüğü PCM örneklerinden
 * hesaplanır (mikrofon izni GEREKMEZ). Ses yoksa/durunca çubuklar söner;
 * "ses varken sesle birlikte" hareket eder.
 */
public class SpectrumView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final float[] shown = new float[8];
    private final int[] colors = {
            Color.parseColor("#FF7A18"), Color.parseColor("#FFA24A"),
            Color.parseColor("#FFD9B0"), Color.parseColor("#E7EEF8")
    };

    public SpectrumView(Context c) { super(c); }

    @Override protected void onDraw(Canvas canvas) {
        float[] target = AudioService.spectrum();
        boolean active = AudioService.isPlayingStatic();
        int n = shown.length;
        float w = getWidth();
        float h = getHeight();
        float slot = w / n;
        float bw = slot * 0.52f;
        for (int i = 0; i < n; i++) {
            float t = active ? target[Math.min(i, target.length - 1)] : 0f;
            // yumuşak iniş/çıkış: göz yormayan ama sesle uyumlu hareket
            shown[i] += (t - shown[i]) * (t > shown[i] ? 0.55f : 0.16f);
            if (shown[i] < 0.02f) shown[i] = 0.02f;
            float bh = h * Math.max(0.04f, Math.min(1f, shown[i]));
            float x = i * slot + (slot - bw) / 2f;
            p.setShader(new LinearGradient(0, h - bh, 0, h,
                    colors[i % 3], Color.parseColor("#B33D00"), Shader.TileMode.CLAMP));
            r.set(x, h - bh, x + bw, h);
            canvas.drawRoundRect(r, bw / 2f, bw / 2f, p);
            p.setShader(null);
            // tepe noktası: canlı yayın hissi
            p.setColor(colors[3]);
            p.setAlpha(180);
            canvas.drawRoundRect(new RectF(x, h - bh - dp(2), x + bw, h - bh), bw / 2f, bw / 2f, p);
            p.setAlpha(255);
        }
        // Çalarken akıcı (60 fps), sessizken yavaş (4 fps) yenile: ilk karede ses
        // henüz gelmemiş olsa bile görünüm bir daha donup kalmaz.
        if (active) postInvalidateOnAnimation();
        else postInvalidateDelayed(250);
    }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
}
