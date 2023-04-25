package com.irlab.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irlab.view.R;
import com.irlab.view.entity.Friend;
import com.irlab.view.listener.OnItemButtonListener;

import java.util.List;

import ru.katso.livebutton.LiveButton;

/*
好友ListView的适配器
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.RecordViewHolder> {

    private final List<Friend> list;  // 数据容器
    private OnItemButtonListener mOnItemButtonListener; // 容器内的单独按钮点击监听器

    public FriendAdapter(List<Friend> list) {
        this.list = list;
    }

    // 设置容器内按钮的监听器
    public void setOnItemButtonListener(OnItemButtonListener buttonListener) {
        this.mOnItemButtonListener = buttonListener;
    }

    // 内部类实现viewHolder 拿到cardView中的布局元素
    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView username, level, online;
        private final LiveButton invite;
        private final View root;

        public RecordViewHolder(View root, final OnItemButtonListener onItemButtonListener) {
            super(root);
            this.root = root;
            username = root.findViewById(R.id.tv_friend_info);
            level = root.findViewById(R.id.tv_friend_level);
            online = root.findViewById(R.id.tv_friend_state);
            invite = root.findViewById(R.id.btn_invite);
            // 为按钮单独设置监听，而不是一整个item
            invite.setOnClickListener(v -> {
                if (null != onItemButtonListener) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemButtonListener.onButtonClicked(v, position);
                    }
                }
            });
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
        return new RecordViewHolder(view, mOnItemButtonListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 好友信息
        holder.username.setText(list.get(position).getUsername());
        holder.level.setText(list.get(position).getLevel());
        holder.online.setText(list.get(position).getOnline() ? "在线" : "不在线");
        if (!list.get(position).getOnline()) {
            holder.invite.setEnabled(false);
            holder.invite.setBackgroundColor(0xffC0C0C0);
            holder.invite.setShadowColor(0xff7f8c8d);
        } else {
            holder.invite.setEnabled(true);
        }

        // 设置tag
        holder.root.setTag(position);
    }
}
