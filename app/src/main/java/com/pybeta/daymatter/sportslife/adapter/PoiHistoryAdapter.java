package com.pybeta.daymatter.sportslife.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pybeta.daymatter.sportslife.R;
import com.pybeta.daymatter.sportslife.bean.PoiObject;

import java.util.List;

/**
 * Created by luogj on 2018/4/10.
 */

public class PoiHistoryAdapter extends RecyclerView.Adapter<PoiHistoryAdapter.MyViewHolder> {
    public Context context;
    OnHistoryItemClickListener listener;
    private List<PoiObject> list;

    public PoiHistoryAdapter(Context context, List<PoiObject> list) {
        this.context = context;
        this.list = list;
    }

    public void changeData(List<PoiObject> list) {
        if (list == null)
            this.list.clear();
        else
            this.list = list;
        notifyDataSetChanged();
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_history_item, null);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) view.getTag();
                if (listener != null) {
                    listener.onHistoryItemClick(view, position,list.get(position));
                }
            }
        });
        return holder;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        PoiObject poi = list.get(position);
        holder.place.setText(poi.address);
        holder.district.setText(poi.district);
        holder.itemView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView place, district;

        public MyViewHolder(View view) {
            super(view);
            place = (TextView) view.findViewById(R.id.place);
            district = (TextView) view.findViewById(R.id.district);
        }
    }

    public interface OnHistoryItemClickListener {
        public void onHistoryItemClick(View v, int position,  PoiObject info);
    }

    public void setOnClickListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }
}
