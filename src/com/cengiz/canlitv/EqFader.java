package com.cengiz.canlitv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/**
 * Dikey ekolayzır fader'ı (v10.02).
 *
 * Eski ekranda 5 adet SeekBar 270° döndürülüyordu: ray koyu zeminde görünmüyor,
 * sadece başlık noktaları kalıyordu ve 170 dp'lik alan boş duruyordu.
 * Bu bileşen kendi çiziyor: dB etiketi, ray, dolgu, tutamak, frekans etiketi.
 * Dokunma/sürükleme doğrudan değeri değiştirir.
 */
public class EqFader extends View {

    public interface OnChange {
        void onChanged(int band, short level);
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    private final int band;
    private final int min;
    private final int max;
    private final String hz;
    private final String db;
    private short level;
    private OnChange cb;

    public EqFader(Context c, int band, int min, int max, short level, String hz) {
        super(c);
        this.band = band;
        this.min = min;
        this.max = max;
        this.hz = hz;
        this.level = level;
        this.db = "0";
        text.setTextAlign(Paint.Align.CENTER);
        setClickable(true);
    }

    public void setOnChange(OnChange c) { this.cb = c; }

    public void setLevel(short lv) {
        if (lv < min) lv = (short) min;
        if (lv > max) lv = (short) max;
        level = lv;
        invalidate();
    }

    public short level() { return level; }

    private int colorOf(String hex) { return Color.parseColor(hex); }

    private float trackTop() { return dpLocal(26); }

    private float trackBottom() { return getHeight() - dpLocal(24); }

    private float trackWidth() { return dpLocal(7); }

    @Override protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float top = trackTop();
        float bottom = trackBottom();

        // --- dB değeri (üstte) ---
        text.setTextSize(dpLocal(9));
        text.setColor(Color.parseColor("#8FA0B5"));
        float dbVal = level / 100f;          // -15..+15 dB (mB -> dB)
        canvas.drawText((dbVal > 0 ? "+" : "") + Math.round(dbVal) + " dB", cx, dpLocal(12), text);

        // --- ray ---
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.parseColor("#0D1A2A"));
        r.set(cx - trackWidth() / 2, top, cx + trackWidth() / 2, bottom);
        canvas.drawRoundRect(r, trackWidth() / 2, trackWidth() / 2, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dpLocal(1.5f));
        p.setColor(Color.parseColor("#3A5580"));
        canvas.drawRoundRect(r, trackWidth() / 2, trackWidth() / 2, p);
        p.setStyle(Paint.Style.FILL);

        // --- dolgu (0 dB çizgisinden itibaren) ---
        float frac = (float) (level - min) / (float) Math.max(1, max - min);
        float knobY = bottom - frac * (bottom - top);
        float zeroY = bottom - (0f - min) / Math.max(1, max - min) * (bottom - top);
        int orange = colorOf("#FF7A18");
        p.setShader(new LinearGradient(0, Math.min(knobY, zeroY), 0, Math.max(knobY, zeroY),
                colorOf("#FFA24A"), colorOf("#C74E00"), Shader.TileMode.CLAMP));
        r.set(cx - trackWidth() / 2, Math.min(knobY, zeroY),
                cx + trackWidth() / 2, Math.max(knobY, zeroY));
        canvas.drawRoundRect(r, trackWidth() / 2, trackWidth() / 2, p);
        p.setShader(null);

        // --- 0 dB referans çizgisi + yan tikler ---
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dpLocal(1));
        p.setColor(Color.parseColor("#41597E"));
        canvas.drawLine(cx - dpLocal(13), zeroY, cx + dpLocal(13), zeroY, p);
        for (int i = 0; i <= 4; i++) {
            float y = top + (bottom - top) * i / 4f;
            canvas.drawLine(cx - dpLocal(10), y, cx - dpLocal(6), y, p);
            canvas.drawLine(cx + dpLocal(6), y, cx + dpLocal(10), y, p);
        }
        p.setStyle(Paint.Style.FILL);

        // --- tutamak ---
        p.setColor(Color.parseColor("#E7EEF8"));
        p.setShadowLayer(dpLocal(4), 0, dpLocal(2), Color.parseColor("#88000000"));
        r.set(cx - dpLocal(15), knobY - dpLocal(6), cx + dpLocal(15), knobY + dpLocal(6));
        canvas.drawRoundRect(r, dpLocal(5), dpLocal(5), p);
        p.clearShadowLayer();
        p.setColor(orange);
        r.set(cx - dpLocal(15), knobY - dpLocal(1.5f), cx + dpLocal(15), knobY + dpLocal(1.5f));
        canvas.drawRoundRect(r, dpLocal(1.5f), dpLocal(1.5f), p);

        // --- frekans etiketi (altta) ---
        text.setTextSize(dpLocal(9));
        text.setColor(Color.parseColor("#C7D2E0"));
        text.setFakeBoldText(true);
        canvas.drawText(hz, cx, h - dpLocal(8), text);
        text.setFakeBoldText(false);
    }

    private float dpLocal(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private void setFromY(float y) {
        float top = trackTop();
        float bottom = trackBottom();
        float frac = (bottom - y) / Math.max(1f, bottom - top);
        if (frac < 0) frac = 0;
        if (frac > 1) frac = 1;
        int lv = Math.round(min + frac * (max - min));
        setLevel((short) lv);
        if (cb != null) cb.onChanged(band, (short) lv);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                setFromY(e.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(e);
    }
}
