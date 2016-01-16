package com.mufans.loopscrollcirclelabel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liujun on 16-1-16.
 * 循环滚动的容器
 */
public class LooperScrollContainer extends ViewGroup {

    private static final String TAG = LooperScrollContainer.class.getName();

    private static final float SCALE_FACTOR = 0.2f; //缩放倍率
    private static final int STATUS_REST = 0;
    private static final int STATUS_MOVE = 1;

    private int status = STATUS_REST;

    private int selectedPos;
    private int childWidth;
    private int childHeight;

    private int offsetX;
    private int downX, downY;
    private int lastX, lastY;
    private int touchSlop;

    private List<View> viewlist = new LinkedList<>();

    public LooperScrollContainer(Context context) {
        super(context);
        init(context);
    }

    public LooperScrollContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LooperScrollContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (getChildCount() > 0) {
            childWidth = getChildAt(0).getMeasuredWidth();
            childHeight = getChildAt(0).getMeasuredHeight();
            height = (int) (childHeight * SCALE_FACTOR + childHeight + 0.5f);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int left = centerX - childWidth / 2;
        int top = centerY - childHeight / 2;
        int frontIndex = 0;
        float maxRatio = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = viewlist.get(i);

//                if (selectedPos == i) {
//                    //child.bringToFront();
//                    child.setScaleX(SCALE_FACTOR);
//                    child.setScaleY(SCALE_FACTOR);
//                } else {
//                    child.setScaleX(1);
//                    child.setScaleY(1);
//                }

            if (i == 0) {
                left += offsetX;
            } else {
                left += childWidth;
            }

            int distance = Math.abs(centerX - (left + childWidth / 2));

            float scaleRatio = distance > childWidth ? 1 : 1 + (childWidth - distance) * 1.0f / childWidth * SCALE_FACTOR;

            if (maxRatio < scaleRatio) {
                maxRatio = scaleRatio;
                frontIndex = i;
            }
            Log.d(TAG, "scaleRatio:" + scaleRatio);
            child.setScaleY(scaleRatio);
            child.setScaleX(scaleRatio);

            child.layout(left, top, left + childWidth, top + childHeight);
        }

        viewlist.get(frontIndex).bringToFront();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            viewlist.add(child);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (status != STATUS_MOVE) {
                    if (Math.abs(x - downX) > touchSlop) {
                        lastX = x;
                        lastY = y;
                        status = STATUS_MOVE;
                        return true;
                    } else {
                        status = STATUS_REST;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                status = STATUS_REST;
        }

        lastX = x;
        lastY = y;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        int action = ev.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (status != STATUS_MOVE) {
                    if (Math.abs(x - downX) > touchSlop) {
                        status = STATUS_MOVE;
                        return true;
                    } else {
                        status = STATUS_REST;
                    }
                } else {
                    moveBy(x - lastX);
                }
                break;
            case MotionEvent.ACTION_UP:

                autoMove();
                status = STATUS_REST;
        }

        lastX = x;
        lastY = y;
        return true;
    }

    private void moveBy(int deltaX) {
        offsetX += deltaX;
        Log.d(TAG, "moveBy:" + deltaX);
        selectedPos = (-2) * offsetX / childWidth;
        if (selectedPos < 0) {
            selectedPos = 0;
        } else if (selectedPos > getChildCount() - 1) {
            selectedPos = getChildCount() - 1;
        }
        Log.d(TAG, "selectedPos:" + selectedPos);
        requestLayout();
    }

    private void autoMove() {
        int from = offsetX;
        int to = from;
        int retainX = offsetX % childWidth;
        Log.d(TAG, "retainX:" + retainX);
        if (Math.abs(retainX) > childWidth / 2) {
            Log.d(TAG, "retainX:>");
            to -= childWidth + retainX;
        } else {
            Log.d(TAG, "retainX:<");
            to -= retainX;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offsetX = (int) animation.getAnimatedValue();
                requestLayout();
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }
}
