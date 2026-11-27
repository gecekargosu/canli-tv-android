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
 * "Cam + neon halka" radyo göstergesi (v10.02, tasarım 2).
 * Halka, istasyonun listedeki yerini gösterir; ses çalarken ince bir nefes efekti verir.
 */
public class RingGauge extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private float progress = 0.25f;      // 0..1
    private boolean live = false;

    public RingGauge(Context c) { super(c); }

    public void setProgress(float v) {
        progress = Math.max(0f, Math.min(1f, v));
        invalidate();
    }

    public void setLive(boolean l) { live = l; }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        // Kutu genisligi yuksekliginden fazla olabilir: halka HER ZAMAN daire kalsin.
        float size = Math.min(w, h);
        float cx = w / 2f;
        float cy = h / 2f;
        float stroke = dp(9);
        float pad = stroke / 2f + dp(2);
        float r = size / 2f - pad;
        arc.set(cx - r, cy - r, cx + r, cy + r);

        // dış halka: koyu ray
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setShader(null);
        p.setColor(Color.parseColor("#1D2A40"));
        canvas.drawArc(arc, 135, 270, false, p);

        // dolu kısım: turuncu -> amber
        p.setShader(new LinearGradient(arc.left, arc.top, arc.right, arc.bottom,
                Color.parseColor("#FF7A18"), Color.parseColor("#FFC078"), Shader.TileMode.CLAMP));
        canvas.drawArc(arc, 135, 270 * (0.06f + progress * 0.94f), false, p);
        p.setShader(null);

        // iç halka (cam hissi)
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(1));
        p.setColor(Color.parseColor(live ? "#33FF7A18" : "#1B2942"));
        float r2 = r - stroke;
        RectF inner = new RectF(cx - r2, cy - r2, cx + r2, cy + r2);
        canvas.drawArc(inner, 0, 360, false, p);

        // çalarken yumuşak parlama
        if (live) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(3));
            p.setColor(Color.parseColor("#22FF7A18"));
            canvas.drawArc(arc, 135, 360, false, p);
        }
    }
}
