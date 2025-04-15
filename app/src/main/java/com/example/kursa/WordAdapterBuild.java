package com.example.kursa;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка слов (WordBuild) в RecyclerView
 */
public class WordAdapterBuild extends RecyclerView.Adapter<WordAdapterBuild.WordViewHolder> {

    private List<WordBuild> words;
    private OnWordClickListener listener;

    /**
     * Конструктор адаптера
     * @param words список слов для отображения
     * @param listener обработчик кликов по словам
     */
    public WordAdapterBuild(List<WordBuild> words, OnWordClickListener listener) {
        this.words = new ArrayList<>(words);
        this.listener = listener;
    }

    /**
     * Создает новый ViewHolder при необходимости
     */
    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    /**
     * Привязывает данные слова к ViewHolder
     * @param holder ViewHolder для заполнения
     * @param position позиция в списке
     */
    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordBuild word = words.get(position);
        holder.wordTextView.setText(word.getText());
        holder.wordTextView.setAlpha(word.isSelected() ? 0.5f : 1.0f);
        holder.wordTextView.setOnClickListener(v -> listener.onWordClick(word));
        Log.d("WordAdapterBuild", "Отображаем слово: " + word.getText() + " на позиции " + position);
    }

    /**
     * Возвращает общее количество слов в списке
     */
    @Override
    public int getItemCount() {
        Log.d("WordAdapterBuild", "Всего слов: " + words.size());
        return words.size();
    }

    /**
     * Обновляет список слов в адаптере
     * @param newWords новый список слов
     */
    public void updateWords(List<WordBuild> newWords) {
        this.words.clear();
        this.words.addAll(newWords);
        Log.d("WordAdapterBuild", "Обновлено слов: " + newWords.size());
        notifyDataSetChanged();
    }

    /**
     * ViewHolder для хранения представления одного элемента списка
     */
    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            if (wordTextView == null) {
                Log.e("WordAdapterBuild", "Не найден wordTextView в макете item_word");
            }
        }
    }

    /**
     * Интерфейс для обработки кликов по словам
     */
    interface OnWordClickListener {
        void onWordClick(WordBuild word);
    }
}