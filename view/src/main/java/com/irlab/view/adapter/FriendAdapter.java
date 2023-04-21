package com.irlab.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irlab.view.R;
import com.irlab.view.entity.Friend;

import java.util.List;

/*
好友ListView的适配器
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.RecordViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    // 数据容器
    private final List<Friend> list;

    private setClick onItemClickListener;
    private setLongClick onItemLongClickListener;

    public FriendAdapter(List<Friend> list) {
        this.list = list;
    }

    // 内部类实现viewHolder 拿到cardView中的布局元素
    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView username, level;
        private final Button invite;
        private final View root;

        public RecordViewHolder(View root) {
            super(root);
            this.root = root;
            username = root.findViewById(R.id.tv_friend_info);
            level = root.findViewById(R.id.tv_friend_level);
            invite = root.findViewById(R.id.btn_invite);
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
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        RecordViewHolder viewHolder = new RecordViewHolder(view);
        // 为Item设置点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 好友信息
        holder.username.setText(list.get(position).getUsername());
        holder.level.setText(list.get(position).getLevel());
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
