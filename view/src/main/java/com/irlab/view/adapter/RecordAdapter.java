package com.irlab.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irlab.view.R;
import com.irlab.view.entity.GameInfo;

import java.util.List;

/*
棋谱ListView的适配器
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    // 数据容器
    private final List<GameInfo> list;

    private setClick onItemClickListener;
    private setLongClick onItemLongClickListener;

    public RecordAdapter(List<GameInfo> list) {
        this.list = list;
    }

    // 内部类实现viewHolder 拿到cardView中的布局元素
    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView playerInfo, date, result;
        private final View root;

        public RecordViewHolder(View root) {
            super(root);
            this.root = root;
            playerInfo = root.findViewById(R.id.tv_player_info);
            date = root.findViewById(R.id.tv_date);
            result = root.findViewById(R.id.tv_result);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        RecordViewHolder viewHolder = new RecordViewHolder(view);
        // 为Item设置点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 双方信息
        holder.playerInfo.setText(list.get(position).getRecordDetail());
        // 日期
        holder.date.setText(list.get(position).getCreateTime());
        // 对局结果
        holder.result.setText(list.get(position).getResult());
        // 设置tag
        holder.root.setTag(position);
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            // 注意这里使用getTag方法获取数据
            onItemClickListener.onItemClickListener(v, (Integer) v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return onItemLongClickListener != null && onItemLongClickListener.onItemLongClickListener(v, (Integer) v.getTag());
    }

    // 设置点击事件
    public void setOnItemClickListener(setClick onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // 设置长按事件
    public void setOnItemLongClickListener(setLongClick onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    // 声明点击和长按接口 将当前item对应的View返回
    public interface setClick {
        void onItemClickListener(View view, int position);
    }

    public interface setLongClick {
        boolean onItemLongClickListener(View view, int position);
    }
}