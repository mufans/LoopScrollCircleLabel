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
 * TODO:1.实现相邻圆缩放时相切 2.处理viewlist的size<SCREEN_SHOW_COUNT的情况 3.缩放参数作为自定义属性
 */
public class LooperScrollContainer extends ViewGroup {

    private static final String TAG = LooperScrollContainer.class.getName();

    private static final int SCREEN_SHOW_COUNT = 5;
    private static final float SCALE_FACTOR = 0.4f; //缩放倍率
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
    private boolean canLoop;
    private int maxCount;
    private int firstVisiblePos;
    private int retainWidth; //容器宽度根据显示的个数等分的余数

    private LooperAdapterWrapper looperAdapterWrapper;

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
        selectedPos = 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        childWidth = width / SCREEN_SHOW_COUNT;
        childHeight = childWidth;
        retainWidth = width - childWidth * SCREEN_SHOW_COUNT;
        Log.d("measurewidth", childWidth + "," + width + "," + childWidth * SCREEN_SHOW_COUNT);
        measureChildren(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY));

        if (getChildCount() > 0) {
//            childWidth = getChildAt(0).getMeasuredWidth();
//            childHeight = getChildAt(0).getMeasuredHeight();
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
        int left = -1 * childWidth;
        int top = centerY - childHeight / 2;
        float maxRatio = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = viewlist.get(i);
            if (i == 0) {
                left += offsetX;
            } else {
                left += childWidth;
            }

            //中间相邻的childView添加间距，调整无法填满的情况
            if (i == getChildCount() / 2 || i == getChildCount() / 2 + 1) {
                left += retainWidth / 2;
            }

            int distance = Math.abs(centerX - (left + childWidth / 2));
            int scaleDistance = width / 2 - childWidth / 2;
            float scaleRatio = 1 + (scaleDistance - distance) * 1.0f / scaleDistance * SCALE_FACTOR;
            if (maxRatio < scaleRatio) {
                maxRatio = scaleRatio;
            }
            if (scaleRatio < 1) {
                scaleRatio = 1;
            }
            Log.d(TAG, "scaleRatio:" + scaleRatio);
            child.setScaleY(scaleRatio);
            child.setScaleX(scaleRatio);

            child.layout(left, top, left + childWidth, top + childHeight);
        }

        reorderZ();


    }

    /**
     * 调整层级
     */
    private void reorderZ() {
        int centerIndex = getChildCount() / 2;
        for (int i = 1; i < centerIndex; i++) {
            viewlist.get(i).bringToFront();
        }
        for (int i = viewlist.size() - 2; i >= centerIndex; i--) {
            viewlist.get(i).bringToFront();
        }
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
        adjustViewOrder();
        requestLayout();
    }

    private void autoMove() {
        int from = offsetX;
        int to = from;
        int retainX = offsetX % childWidth;
        Log.d(TAG, "retainX:" + retainX);
        if (Math.abs(retainX) > childWidth / 2) {
            Log.d(TAG, "retainX:>");
            if (retainX > 0) {
                to += childWidth - retainX;
            } else {
                to -= childWidth + retainX;
            }
        } else {
            Log.d(TAG, "retainX:<");
            to -= retainX;


        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offsetX = (int) animation.getAnimatedValue();
                if (animation.getAnimatedFraction() == 1) {
                    adjustViewOrder();
                    offsetX = 0;
                }
                requestLayout();
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    private void adjustViewOrder() {
        View view = null;
        if (offsetX <= -1 * childWidth) {
            view = viewlist.get(0);
            viewlist.remove(0);
            viewlist.add(view);
            offsetX += childWidth;
            selectedPos++;
        } else if (offsetX >= childWidth) {
            view = viewlist.remove(viewlist.size() - 1);
            viewlist.add(0, view);
            offsetX -= childWidth;
            selectedPos--;
        }

        int dataCount = looperAdapterWrapper.getDataCount();

        Log.d(TAG, "anim:before" + selectedPos);
        if (selectedPos >= dataCount) {
            selectedPos = 0;
        } else if (selectedPos < 0) {

            selectedPos += dataCount;
        }

        firstVisiblePos = selectedPos - 2;
        if (firstVisiblePos < 0) {
            firstVisiblePos += dataCount;
        }
        if (view != null) {
            int pos = viewlist.indexOf(view);
            Log.d(TAG, "anim:" + selectedPos + ":" + firstVisiblePos + ":" + pos);
            looperAdapterWrapper.getItemView(pos, firstVisiblePos, view, LooperScrollContainer.this);
        }


    }

    public void setAdapter(BaseAdapter adapter) {
        viewlist.clear();
        removeAllViews();
        looperAdapterWrapper = new LooperAdapterWrapper(SCREEN_SHOW_COUNT, adapter);
        for (int i = 0; i < looperAdapterWrapper.getCount(); i++) {
            View itemView = looperAdapterWrapper.getItemView(i, firstVisiblePos, null, this);
            viewlist.add(itemView);
            addView(itemView);
        }
        offsetX = 0;
        requestLayout();
    }
}
