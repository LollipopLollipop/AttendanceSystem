package com.example.jodiezz.simplerecord;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Object;
import android.os.Handler;

/**
 * Created by JodiezZ on 3/22/15.
 */
public class InteractionView extends View {
    private Timer timer;
    private Handler handler;
    private final static int UI_UPDATE_MS = 100;
    private byte[] realtimeAudioData;

    private final static int HEADSTOCK_HEIGHT = 10;
    private final static int HEADSTOCK_WIDTH = 50;
    private final static int MIN_AMPLITUDE = 50;
    private final static int MAX_AMPLITUDE = 500;

    private static final String TAG = "IVActivity";

    public InteractionView(Context context) {
        super(context);
        // UI update cycle.
        handler = new Handler();
        timer = new Timer();
        timer.schedule(new TimerTask() {
                           public void run() {
                               handler.post(new Runnable() {
                                   public void run() {
                                       invalidate();
                                   }
                               });
                           }
                       },
                UI_UPDATE_MS,
                UI_UPDATE_MS);
    }


    public void updateRealtimeData(byte[] newDataArr) {
        this.realtimeAudioData = newDataArr;
    }

    private void DrawWaveForm(Canvas canvas, Rect rect) {
        if (this.realtimeAudioData == null) {
            return;
        }
        Paint paint = new Paint();
        // Draw border.
        paint.setARGB(80, 236, 236, 238);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);

        /* Draw threshold.
        paint.setARGB(180, 200, 0, 0);
        final long threshold_screen_height = GetAmplitudeScreenHeight(canvas, MIN_AMPLITUDE, rect);
        canvas.drawLine(rect.left, rect.bottom - threshold_screen_height, rect.right, rect.bottom - threshold_screen_height, paint);
		*/
        // Draw histogram.
        paint.setARGB(255, 192, 108, 132);

        int size = realtimeAudioData.length;
        for(int i = 0; i < size; i ++){
            byte curData = realtimeAudioData[i];

            final double amplitude = Math.min(curData, MAX_AMPLITUDE);
            final long height = GetAmplitudeScreenHeight(canvas, amplitude, rect);
            final long topValue = rect.bottom - height;
            final long leftValue = rect.left + rect.width() * i / size;
            final long rightValue = rect.left + rect.width() * (i + 1) / size;
            final long bottomValue = rect.bottom;
            canvas.drawRect(leftValue, topValue, rightValue, bottomValue, paint);

        }
        return;
    }

    private long GetAmplitudeScreenHeight(Canvas canvas, double amplitude, Rect histogramRect) {
        return Math.round(amplitude / MAX_AMPLITUDE * histogramRect.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        /*if (!showResult) {
            final int MARGIN = 5;
            final int effectiveHeight = canvas.getHeight() - 4 * MARGIN;
            final int effectiveWidth = canvas.getWidth() - 2 * MARGIN;

            final Rect histogram = new Rect(MARGIN, 2 * MARGIN,
                    effectiveWidth + MARGIN, effectiveHeight + MARGIN*2);
            DrawWaveForm(canvas, histogram);
            return;
        }
        else{
            //DrawResult(canvas);
            return;
        }*/
        final int MARGIN = 5;
        final int effectiveHeight = canvas.getHeight() - 4 * MARGIN;
        final int effectiveWidth = canvas.getWidth() - 2 * MARGIN;

        final Rect histogram = new Rect(MARGIN, 2 * MARGIN,
                effectiveWidth + MARGIN, effectiveHeight + MARGIN*2);
        DrawWaveForm(canvas, histogram);
        return;
    }

    public void stopUpdate(){
        timer.cancel();
    }


}
