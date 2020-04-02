package com.hokming.climbmate.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hokming.climbmate.R;

/**
 * Date:        2020/4/2
 * Author:      xueming
 * Describe:
 */
public class CircularProgressView extends View {

    private Paint backPaint, progPaint;   // draw paint
    private RectF rectF;       // draw area
    private int[] colorArray;  // hoop color range
    private int progress;      // hoop progress

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("Recycle")
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView);

        // init background hoop paint
        backPaint = new Paint();
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeCap(Paint.Cap.ROUND);
        backPaint.setAntiAlias(true);
        backPaint.setDither(true);
        backPaint.setStrokeWidth(typedArray.getDimension(R.styleable.CircularProgressView_backWidth, 5));
        backPaint.setColor(typedArray.getColor(R.styleable.CircularProgressView_backColor, Color.LTGRAY));

        // init progress hoop paint
        progPaint = new Paint();
        progPaint.setStyle(Paint.Style.STROKE);
        progPaint.setStrokeCap(Paint.Cap.ROUND);
        progPaint.setAntiAlias(true);
        progPaint.setDither(true);
        progPaint.setStrokeWidth(typedArray.getDimension(R.styleable.CircularProgressView_progWidth, 10));
        progPaint.setColor(typedArray.getColor(R.styleable.CircularProgressView_progColor, Color.BLUE));

        // init progress hoop colorarray
        int startColor = typedArray.getColor(R.styleable.CircularProgressView_progStartColor, -1);
        int firstColor = typedArray.getColor(R.styleable.CircularProgressView_progFirstColor, -1);
        if (startColor != -1 && firstColor != -1) colorArray = new int[]{startColor, firstColor};
        else colorArray = null;

        // 初始化进度
        progress = typedArray.getInteger(R.styleable.CircularProgressView_progress, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWide = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int viewHigh = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int mRectLength = (int) ((Math.min(viewWide, viewHigh)) - (Math.max(backPaint.getStrokeWidth(), progPaint.getStrokeWidth())));
        int mRectL = getPaddingLeft() + (viewWide - mRectLength) / 2;
        int mRectT = getPaddingTop() + (viewHigh - mRectLength) / 2;
        rectF = new RectF(mRectL, mRectT, mRectL + mRectLength, mRectT + mRectLength);

        // set progress colorarray
        if (colorArray != null && colorArray.length > 1)
            progPaint.setShader(new LinearGradient(0, 0, 0, getMeasuredWidth(), colorArray, null, Shader.TileMode.MIRROR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rectF, 0, 360, false, backPaint);
        canvas.drawArc(rectF, 275, 360 * progress / 100, false, progPaint);
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setProgress(int progress, long animTime) {
        if (animTime <= 0) setProgress(progress);
        else {
            ValueAnimator animator = ValueAnimator.ofInt(this.progress, progress);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    CircularProgressView.this.progress = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.setInterpolator(new OvershootInterpolator());
            animator.setDuration(animTime);
            animator.start();
        }
    }


    public void setBackWidth(int width) {
        backPaint.setStrokeWidth(width);
        invalidate();
    }


    public void setBackColor(@ColorRes int color) {
        backPaint.setColor(ContextCompat.getColor(getContext(), color));
        invalidate();
    }

    public void setProgWidth(int width) {
        progPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setProgColor(@ColorRes int color) {
        progPaint.setColor(ContextCompat.getColor(getContext(), color));
        progPaint.setShader(null);
        invalidate();
    }


    public void setProgColor(@ColorRes int startColor, @ColorRes int firstColor) {
        colorArray = new int[]{ContextCompat.getColor(getContext(), startColor), ContextCompat.getColor(getContext(), firstColor)};
        progPaint.setShader(new LinearGradient(0, 0, 0, getMeasuredWidth(), colorArray, null, Shader.TileMode.MIRROR));
        invalidate();
    }


    public void setProgColor(@ColorRes int[] colorArray) {
        if (colorArray == null || colorArray.length < 2) return;
        this.colorArray = new int[colorArray.length];
        for (int index = 0; index < colorArray.length; index++)
            this.colorArray[index] = ContextCompat.getColor(getContext(), colorArray[index]);
        progPaint.setShader(new LinearGradient(0, 0, 0, getMeasuredWidth(), this.colorArray, null, Shader.TileMode.MIRROR));
        invalidate();
    }
}
