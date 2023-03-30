package com.irlab.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.irlab.view.R;
import com.irlab.view.wifi.WifiListBean;

import java.util.List;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.MyViewHolder> {
    private final List<WifiListBean> wifiListBeanList;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public WifiAdapter(List<WifiListBean> wifiListBeanList) {
        this.wifiListBeanList = wifiListBeanList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_main, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tv_name.setText("wifi名：" + wifiListBeanList.get(position).getName());
        holder.tv_encrypt.setText("加密方式：" + wifiListBeanList.get(position).getEncrypt());
        holder.btn_link.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, position));
    }

    @Override
    public int getItemCount() {
        return wifiListBeanList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name, tv_encrypt;
        Button btn_link;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_encrypt = view.findViewById(R.id.tv_encrypt);
            btn_link = view.findViewById(R.id.btn_link);
        }
    }
}
