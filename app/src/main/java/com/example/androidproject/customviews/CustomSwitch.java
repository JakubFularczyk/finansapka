package com.example.androidproject.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.graphics.drawable.GradientDrawable;

public class CustomSwitch extends SwitchCompat {

    // Stałe
    private static final int TRACK_COLOR = 0xFFFFFFFF;
    private static final float TRACK_STROKE_WIDTH = dpToPx(2);
    private static final int TRACK_STROKE_COLOR = 0xFF00A1FF;
    private static final int TRACK_LABEL_COLOR = 0xFF00A1FF;
    private static final float TRACK_LABEL_SIZE = spToPx(14);

    private static final int THUMB_COLOR = 0xFF00A1FF;
    private static final int THUMB_LABEL_COLOR = 0xFFFFFFFF;
    private static final float THUMB_LABEL_SIZE = spToPx(14);

    // Farby do rysowania etykiet
    private final Paint trackLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CustomSwitch(Context context) {
        super(context);
        init();
    }

    public CustomSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Ustawienia farb
        trackLabelPaint.setTextSize(TRACK_LABEL_SIZE);
        trackLabelPaint.setColor(TRACK_LABEL_COLOR);

        thumbLabelPaint.setTextSize(THUMB_LABEL_SIZE);
        thumbLabelPaint.setColor(THUMB_LABEL_COLOR);

        // Ustawienia wyglądu
        setBackground(null);
        setTrackDrawable(new TrackDrawable());
        setThumbDrawable(new ThumbDrawable());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        GradientDrawable trackDrawable = (GradientDrawable) getTrackDrawable();
        if (trackDrawable != null) {
            trackDrawable.setSize(w, h);
        }

        GradientDrawable thumbDrawable = (GradientDrawable) getThumbDrawable();
        if (thumbDrawable != null) {
            thumbDrawable.setSize(w / 2, h);
        }
    }

    private static float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    private static float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }

    private static void drawLabel(Canvas canvas, Rect bounds, Paint paint, CharSequence text) {
        if (text == null) return;

        RectF textBounds = new RectF();
        textBounds.right = paint.measureText(text, 0, text.length());
        textBounds.bottom = paint.descent() - paint.ascent();
        textBounds.left += bounds.centerX() - textBounds.centerX();
        textBounds.top += bounds.centerY() - textBounds.centerY() - paint.ascent();

        canvas.drawText(text.toString(), textBounds.left, textBounds.top, paint);
    }

    private CharSequence getThumbLabel() {
        return isChecked() ? getTextOn() : getTextOff();
    }

    private class TrackDrawable extends GradientDrawable {
        private final Rect textOffBounds = new Rect();
        private final Rect textOnBounds = new Rect();

        public TrackDrawable() {
            setColor(TRACK_COLOR);
            setStroke((int) TRACK_STROKE_WIDTH, TRACK_STROKE_COLOR);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);

            setCornerRadius(bounds.height() / 2f);

            textOffBounds.set(bounds);
            textOffBounds.right /= 2;

            textOnBounds.set(textOffBounds);
            textOnBounds.offset(textOffBounds.right, 0);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            drawLabel(canvas, textOffBounds, trackLabelPaint, getTextOff());
            drawLabel(canvas, textOnBounds, trackLabelPaint, getTextOn());
            invalidate();
            requestLayout();
        }
    }

    private class ThumbDrawable extends GradientDrawable {
        private final Rect thumbLabelBounds = new Rect();

        public ThumbDrawable() {
            setColor(THUMB_COLOR);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            setCornerRadius(bounds.height() / 2f);
            thumbLabelBounds.set(bounds);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            drawLabel(canvas, thumbLabelBounds, thumbLabelPaint, getThumbLabel());
            invalidate();
            requestLayout();
        }
    }
}
