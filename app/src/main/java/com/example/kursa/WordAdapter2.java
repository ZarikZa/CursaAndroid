
package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
/**
 * WordAdapter2 — адаптер для отображения списка слов в RecyclerView.
 * Показывает английские слова и их переводы без дополнительных функций,
 * таких как удаление.
 */
public class WordAdapter2 extends RecyclerView.Adapter<WordAdapter2.ViewHolder> {
    private final List<Word> wordList;

    /**
     * Конструктор адаптера.
     *
     * @param wordList Список слов для отображения
     */
    public WordAdapter2(List<Word> wordList) {
        this.wordList = wordList;
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
        return new ViewHolder(view);
    }

    /**
     * Связывает данные слова с ViewHolder.
     *
     * @param holder   ViewHolder для элемента
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.englishWordTextView.setText(word.getEnglish());
        holder.translationTextView.setText(word.getTranslation());
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
     * ViewHolder для элемента списка, содержащий поля для слова и перевода.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView englishWordTextView;
        public final TextView translationTextView;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление элемента
         */
        public ViewHolder(View itemView) {
            super(itemView);
            englishWordTextView = itemView.findViewById(R.id.englishWordTextView);
            translationTextView = itemView.findViewById(R.id.translationTextView);
        }
    }
}