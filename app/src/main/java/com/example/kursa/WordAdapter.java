package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
/**
 * WordAdapter — адаптер для отображения списка слов в словаре.
 * Используется в RecyclerView для показа английских слов, их переводов и кнопки удаления
 * для сложных слов. Поддерживает обработку нажатий на кнопку удаления через слушатель.
 */
public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private List<WordLevel> wordList;
    private OnDeleteClickListener deleteClickListener;

    /**
     * Интерфейс для обработки нажатий на кнопку удаления.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(WordLevel word);
    }

    /**
     * Конструктор адаптера.
     *
     * @param wordList            Список слов для отображения
     * @param deleteClickListener Слушатель для обработки удаления слов
     */
    public WordAdapter(List<WordLevel> wordList, OnDeleteClickListener listener) {
        this.wordList = wordList;
        this.deleteClickListener = listener;
    }

    /**
     * Создает новый ViewHolder для элемента списка.
     *
     * @param parent   Родительская ViewGroup
     * @param viewType Тип представления
     * @return Новый экземпляр ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_for_slovar, parent, false);
        return new ViewHolder(view, deleteClickListener);
    }

    /**
     * Связывает данные слова с ViewHolder.
     *
     * @param holder   ViewHolder для элемента
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordLevel word = wordList.get(position);
        holder.bind(word);
    }

    /**
     * Возвращает количество слов в списке.
     *
     * @return Размер списка слов
     */
    @Override
    public int getItemCount() {
        return wordList.size();
    }

    /**
     * ViewHolder для элемента списка, содержащий поля для слова, перевода и кнопки удаления.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView englishWordTextView;
        private final TextView translationTextView;
        private final ImageButton deleteButton;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView            Представление элемента
         * @param deleteClickListener Слушатель для обработки удаления
         */
        public ViewHolder(View itemView, OnDeleteClickListener deleteClickListener) {
            super(itemView);
            englishWordTextView = itemView.findViewById(R.id.englishWordTextView);
            translationTextView = itemView.findViewById(R.id.translationTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteClickListener.onDeleteClick((WordLevel) itemView.getTag());
                }
            });
        }

        /**
         * Связывает данные слова с элементами интерфейса.
         *
         * @param word Объект слова
         */
        public void bind(WordLevel word) {
            itemView.setTag(word);
            englishWordTextView.setText(word.getEnglish());
            translationTextView.setText(word.getTranslation());
            deleteButton.setVisibility(word.isHard() ? View.VISIBLE : View.GONE);
        }
    }
}