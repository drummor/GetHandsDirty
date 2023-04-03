package com.android.sample.gethandsdirty.frames.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.android.sample.gethandsdirty.R;

import java.util.LinkedList;

public class FrameLineChartView extends View {

    private final Paint paint;
    private final TextPaint tipPaint;

    private final TextPaint fpsPaint;

    private final Paint levelLinePaint;
    private final Paint tipLinePaint;
    private final static int LINE_COUNT = 50;
    private final LinkedList<LineInfo> lines;
    float linePadding;
    float lineStrokeWidth;
    private Path topPath = new Path();
    private Path middlePath = new Path();
    private float[] topTip = new float[2];
    private float[] middleTip = new float[2];

    private int bestColor = getContext().getResources().getColor(R.color.level_best_color);
    private int normalColor = getContext().getResources().getColor(R.color.level_normal_color);
    private int middleColor = getContext().getResources().getColor(R.color.level_middle_color);
    private int highColor = getContext().getResources().getColor(R.color.level_high_color);
    private int frozenColor = getContext().getResources().getColor(R.color.level_frozen_color);

    private int grayColor = getContext().getResources().getColor(R.color.dark_text);
    float padding = dip2px(getContext(), 8);
    float width;
    float lineContentHeight;

    float height;
    float textSize;

    public FrameLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        fpsPaint = new TextPaint();

        fpsPaint.setTextSize(textSize = dip2px(getContext(), 12));
        fpsPaint.setStrokeWidth(dip2px(getContext(), 1));


        paint = new Paint();
        tipPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setTextSize(textSize = dip2px(getContext(), 10));
        tipPaint.setStrokeWidth(dip2px(getContext(), 1));
        tipPaint.setColor(grayColor);

        levelLinePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        levelLinePaint.setStrokeWidth(dip2px(getContext(), 1));
        levelLinePaint.setStyle(Paint.Style.STROKE);

        levelLinePaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));

        tipLinePaint = new Paint(tipPaint);
        tipLinePaint.setStrokeWidth(dip2px(getContext(), 1));
        tipLinePaint.setColor(grayColor);
        tipLinePaint.setStyle(Paint.Style.STROKE);
        tipLinePaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));
        lines = new LinkedList<>();
    }

    public FrameLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();

            //完成线的高度
            lineContentHeight = height - 3 * padding;

            //线的宽度
            lineStrokeWidth = dip2px(getContext(), 3);
            paint.setStrokeWidth(lineStrokeWidth);
            //2dp
            linePadding = lineStrokeWidth * 2;

            //分割成60段
            float rate = lineContentHeight / 60;

            //顶部的线
            //X
            topTip[0] = LINE_COUNT * linePadding + padding;
            //y
            topTip[1] = 10 * rate + (height - lineContentHeight);

            topPath.moveTo(topTip[0], topTip[1]);
            topPath.lineTo(0, topTip[1]);
            //中间的线
            middleTip[1] = 30 * rate + (height - lineContentHeight);
            middleTip[0] = LINE_COUNT * linePadding + padding;

            middlePath.moveTo(middleTip[0], middleTip[1]);
            middlePath.lineTo(0, middleTip[1]);
        }
    }

    public void addFps(int fps) {
        int color = getColor(fps);
        LineInfo linePoint = new LineInfo(fps, color);
        if (lines.size() >= LINE_COUNT) {
            lines.removeLast();
        }
        lines.addFirst(linePoint);
        invalidate();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int index = 1;
        int sumFps = 0;
        for (LineInfo lineInfo : lines) {
            sumFps += lineInfo.fps;
            lineInfo.draw(canvas, index);
            if (index % 25 == 0) {
                Path path = new Path();
                float pathX = lineInfo.linePoint[1];
                path.moveTo(pathX, 0);
                path.lineTo(pathX, getMeasuredHeight());
                canvas.drawPath(path, tipLinePaint);
                tipPaint.setColor(grayColor);
                canvas.drawText(index / 5 + "s", pathX + textSize, padding * 2, tipPaint);
                if (index > 0) {
                    int aver = sumFps / index;
                    tipPaint.setColor(getColor(aver));
                    canvas.drawText(aver + "FPS", pathX - textSize / 2, padding, tipPaint);
                }
            }
            index++;
        }

        tipPaint.setColor(grayColor);
        levelLinePaint.setColor(normalColor);
        canvas.drawPath(topPath, levelLinePaint);
        canvas.drawText("50", topTip[0] - textSize / 2, topTip[1] + textSize, tipPaint);

        levelLinePaint.setColor(middleColor);
        canvas.drawPath(middlePath, levelLinePaint);
        canvas.drawText("30", middleTip[0] - textSize / 2, middleTip[1] + textSize, tipPaint);

        if (!lines.isEmpty()) {
            LineInfo first = lines.getFirst();

            fpsPaint.setColor(Color.GREEN);

            canvas.drawText(first.fps + "FPS", padding, padding * 1.5f, fpsPaint);
        }

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    class LineInfo {
        private float[] linePoint = new float[4];

        LineInfo(int fps, int color) {
            this.fps = fps;
            this.color = color;
            // startY
            linePoint[0] = height;
            // endY
            linePoint[2] = (60 - fps) * (lineContentHeight) / 60 + (getHeight() - lineContentHeight);

        }

        void draw(Canvas canvas, int index) {
            if (paint.getColor() != color) {
                paint.setColor(color);
            }
            //startX
            linePoint[1] = (1 + index) * linePadding; //
            //endX
            linePoint[3] = linePoint[1];
            canvas.drawLine(linePoint[1], linePoint[0], linePoint[3], linePoint[2], paint);
        }

        int fps;
        int color;
    }

    private int getColor(int fps) {
        int color;
        if (fps > 60 - Constants.DEFAULT_DROPPED_NORMAL) {
            color = bestColor;
        } else if (fps > 60 - Constants.DEFAULT_DROPPED_MIDDLE) {
            color = normalColor;
        } else if (fps > 60 - Constants.DEFAULT_DROPPED_HIGH) {
            color = middleColor;
        } else if (fps > 60 - Constants.DEFAULT_DROPPED_FROZEN) {
            color = highColor;
        } else {
            color = frozenColor;
        }
        return color;
    }
}
