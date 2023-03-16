package com.irlab.view.adapter;

import static com.irlab.base.utils.SPUtils.getInt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irlab.view.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    // 数据容器
    private final List<String> configList;
    private static final String[] LEVELS = {"10级", "9级", "8级", "7级", "6级", "5级", "4级", "3级", "2级", "1级",
            "业余1段", "业余2段", "业余3段", "业余4段", "业余5段", "业余6段", "业余强豪", "职业棋手", "全国冠军", "世界冠军"};
    public static final int[] LEVEL_ICONS = {R.drawable.level_1, R.drawable.level_2,
            R.drawable.level_3, R.drawable.level_4, R.drawable.level_5, R.drawable.level_6, R.drawable.level_7,
            R.drawable.level_8, R.drawable.level_9, R.drawable.level_10, R.drawable.level_11, R.drawable.level_12,
            R.drawable.level_13, R.drawable.level_14, R.drawable.level_15, R.drawable.level_16, R.drawable.level_17,
            R.drawable.level_18, R.drawable.level_19, R.drawable.level_20};

    private int mPosition = 0;

    public RecyclerViewAdapter(List<String> configList) {
        this.configList = configList;
    }

    private setClick onItemClickListener;
    private setLongClick onItemLongClickListener;

    // 内部类实现viewHolder 拿到cardView中的布局元素
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView check, level_icon;
        private final TextView level;
        private final View root;

        public ViewHolder(View root) {
            super(root);
            this.root = root;
            check = root.findViewById(R.id.iv_check);
            level = root.findViewById(R.id.level);
            level_icon = root.findViewById(R.id.level_icon);
        }
    }

    @Override
    public int getItemCount() {
        return configList.size();
    }

    public int getmPosition() { return this.mPosition; }

    public void setmPosition(int mPosition) { this.mPosition = mPosition; }

    // 绑定视图管理者
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mPosition = getInt("level_position");
        holder.level_icon.setImageResource(LEVEL_ICONS[position]);
        holder.level.setText(LEVELS[position]);
        // 设置tag
        holder.root.setTag(position);
        if (position == getmPosition()) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_setting_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        // 为Item设置点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return viewHolder;
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
