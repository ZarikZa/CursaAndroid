package com.example.kursa;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelViewHolder> {
    private List<Level> levels;
    private OnLevelClickListener onLevelClickListener;

    public LevelsAdapter(List<Level> levels, OnLevelClickListener onLevelClickListener) {
        this.levels = levels;
        this.onLevelClickListener = onLevelClickListener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new LevelViewHolder(view, onLevelClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = levels.get(position);
        holder.bind(level);
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    public static class LevelViewHolder extends RecyclerView.ViewHolder {
        private Button levelButton;
        private OnLevelClickListener onLevelClickListener;

        public LevelViewHolder(@NonNull View itemView, OnLevelClickListener onLevelClickListener) {
            super(itemView);
            levelButton = itemView.findViewById(R.id.button);
            this.onLevelClickListener = onLevelClickListener;
        }

        public void bind(Level level) {
            levelButton.setText(level.getLevelName());
            levelButton.setEnabled(level.isUnlocked());
            if (level.isUnlocked()) {
                levelButton.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(R.color.unlocked_level_color));
                levelButton.setTextColor(itemView.getContext().getResources().getColorStateList(R.color.selected_text_color));
            } else {
                levelButton.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(R.color.locked_level_color));
                levelButton.setTextColor(itemView.getContext().getResources().getColorStateList(R.color.selected_text_color));
            }

            levelButton.setOnClickListener(v -> {
                if (onLevelClickListener != null) {
                    onLevelClickListener.onLevelClick(level);
                }
            });
        }
    }

    public interface OnLevelClickListener {
        void onLevelClick(Level level);
    }
}