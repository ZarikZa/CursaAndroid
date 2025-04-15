
package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * LevelsAdapter — адаптер для отображения списка уровней в RecyclerView.
 * Управляет списком объектов Level, отображает их в виде кнопок и обрабатывает
 * взаимодействие с уровнями через интерфейс OnLevelClickListener.
 */
public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelViewHolder> {
    private List<Level> levels;
    private OnLevelClickListener onLevelClickListener;

    public LevelsAdapter(List<Level> levels, OnLevelClickListener onLevelClickListener) {
        this.levels = levels;
        this.onLevelClickListener = onLevelClickListener;
    }

    /**
     * Создает новый ViewHolder для элемента уровня.
     *
     * @param parent   Родительская ViewGroup
     * @param viewType Тип представления
     * @return Новый экземпляр LevelViewHolder
     */
    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new LevelViewHolder(view, onLevelClickListener);
    }

    /**
     * Связывает данные уровня с ViewHolder.
     *
     * @param holder   ViewHolder для элемента
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = levels.get(position);
        holder.bind(level);
    }

    /**
     * Возвращает количество уровней в списке.
     *
     * @return Размер списка уровней
     */
    @Override
    public int getItemCount() {
        return levels.size();
    }

    /**
     * ViewHolder для элемента уровня, содержащий кнопку и логику отображения.
     */
    public static class LevelViewHolder extends RecyclerView.ViewHolder {
        private Button levelButton;
        private OnLevelClickListener onLevelClickListener;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView            Представление элемента
         * @param onLevelClickListener Слушатель кликов по уровням
         */
        public LevelViewHolder(@NonNull View itemView, OnLevelClickListener onLevelClickListener) {
            super(itemView);
            levelButton = itemView.findViewById(R.id.button);
            this.onLevelClickListener = onLevelClickListener;
        }

        /**
         * Связывает данные уровня с элементом интерфейса.
         *
         * @param level Объект уровня
         */
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

    /**
     * Интерфейс для обработки кликов по уровням.
     */
    public interface OnLevelClickListener {
        /**
         * Вызывается при клике на уровень.
         *
         * @param level Выбранный уровень
         */
        void onLevelClick(Level level);
    }
}