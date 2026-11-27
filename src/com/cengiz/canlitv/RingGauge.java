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
    private final RectF inner = new RectF();
    // H-04: onDraw içinde allocation/parseColor yok — renkler ve gradyan önbellekte.
    private static final int C_RAIL = Color.parseColor("#1D2A40");
    private static final int C_GRAD_A = Color.parseColor("#FF7A18");
    private static final int C_GRAD_B = Color.parseColor("#FFC078");
    private static final int C_LIVE = Color.parseColor("#33FF7A18");
    private static final int C_IDLE = Color.parseColor("#1B2942");
    private static final int C_GLOW = Color.parseColor("#22FF7A18");
    private LinearGradient grad;
    private float gradSize = -1f;
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
        p.setColor(C_RAIL);
        canvas.drawArc(arc, 135, 270, false, p);

        // dolu kısım: turuncu -> amber (gradyan boyut değişmedikçe yeniden üretilmez)
        if (grad == null || Math.abs(gradSize - size) > 1f) {
            grad = new LinearGradient(arc.left, arc.top, arc.right, arc.bottom,
                    C_GRAD_A, C_GRAD_B, Shader.TileMode.CLAMP);
            gradSize = size;
        }
        p.setShader(grad);
        canvas.drawArc(arc, 135, 270 * (0.06f + progress * 0.94f), false, p);
        p.setShader(null);

        // iç halka (cam hissi)
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(1));
        p.setColor(live ? C_LIVE : C_IDLE);
        float r2 = r - stroke;
        inner.set(cx - r2, cy - r2, cx + r2, cy + r2);
        canvas.drawArc(inner, 0, 360, false, p);

        // çalarken yumuşak parlama
        if (live) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(3));
            p.setColor(C_GLOW);
            canvas.drawArc(arc, 135, 360, false, p);
        }
    }
}
