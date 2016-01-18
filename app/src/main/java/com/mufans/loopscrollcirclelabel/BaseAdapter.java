package com.mufans.loopscrollcirclelabel;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liujun on 16-1-17.
 * 适配器基类
 */
public abstract class BaseAdapter {

    /**
     * 获取数据个数
     *
     * @return
     */
    public abstract int getCount();

    /**
     * 获取itemView
     *
     * @param pos
     * @param itemView
     * @param containerView
     * @return
     */
    public abstract View getItemView(int pos, View itemView, ViewGroup containerView);

}
