package com.irlab.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.irlab.view.R;
import com.irlab.view.activity.GameRecordActivity;
import com.irlab.view.activity.ManageDeviceActivity;
import com.irlab.view.activity.PlayActivity;
import com.irlab.view.activity.SelectConfigActivity;
import com.irlab.view.activity.ConnectWifiActivity;
import com.irlab.view.activity.SpeechActivity;
import com.irlab.view.bean.MyFunction;

import java.util.List;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder> {

    private Context context;
    private final List<MyFunction> funcList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView textView;
        ImageView service_status_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageView = itemView.findViewById(R.id.func_image);
            textView = itemView.findViewById(R.id.func_name);
            service_status_image = itemView.findViewById(R.id.service_status);
        }
    }

    public FunctionAdapter(List<MyFunction> list) {
        funcList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
            Log.d("onCreateViewHolder", "context == null");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.function_item_layout, parent, false);
        // 添加cardView点击响应
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            MyFunction function = funcList.get(position);
            if (function.getName().equals("开始对弈")) {
                Intent intent = new Intent(context, PlayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (function.getName().equals("选择棋力")) {
                Intent intent = new Intent(context, SelectConfigActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (function.getName().equals("我的对局")) {
                Intent intent = new Intent(context, GameRecordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (function.getName().equals("连接WiFi")) {
                Intent intent = new Intent(context, ConnectWifiActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (function.getName().equals("语音对话")) {
                Intent intent = new Intent(context, SpeechActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (function.getName().equals("我的棋盘")) {
                Intent intent = new Intent(context, ManageDeviceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MyFunction function = funcList.get(position);
        holder.textView.setText(function.getName());
        Glide.with(context).load(function.getImageId()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return funcList.size();
    }
}
