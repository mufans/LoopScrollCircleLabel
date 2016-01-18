package com.mufans.loopscrollcirclelabel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by liujun on 16-1-17.
 */
public class CircleAdapter extends BaseAdapter {


    private Context context;
    private LayoutInflater layoutInflater;
    private List<CircleItem> data;

    public CircleAdapter(Context context, List<CircleItem> data) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public View getItemView(int pos, View itemView, ViewGroup containerView) {

        ViewHolder viewHolder = null;

        if (itemView == null) {
            itemView = layoutInflater.inflate(R.layout.circle_item, containerView,false);
            viewHolder = new ViewHolder();
            viewHolder.itemView = itemView;
            viewHolder.textView = (TextView) itemView.findViewById(R.id.txt);
            itemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) itemView.getTag();
        }
        CircleItem circleItem = data.get(pos);
        viewHolder.itemView.setBackgroundResource(circleItem.color);
        viewHolder.textView.setText(circleItem.txt);
        return itemView;
    }

    private static class ViewHolder {
        private View itemView;
        private TextView textView;
    }
}
