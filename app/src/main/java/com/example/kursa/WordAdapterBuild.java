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

public class WordAdapterBuild extends RecyclerView.Adapter<WordAdapterBuild.WordViewHolder> {

    private List<WordBuild> words;
    private OnWordClickListener listener;

    public WordAdapterBuild(List<WordBuild> words, OnWordClickListener listener) {
        this.words = new ArrayList<>(words); // Копируем список
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordBuild word = words.get(position);
        holder.wordTextView.setText(word.getText());
        holder.wordTextView.setAlpha(word.isSelected() ? 0.5f : 1.0f);
        holder.wordTextView.setOnClickListener(v -> listener.onWordClick(word));
        Log.d("WordAdapterBuild", "Binding word: " + word.getText() + " at position " + position);
    }

    @Override
    public int getItemCount() {
        Log.d("WordAdapterBuild", "Item count: " + words.size());
        return words.size();
    }

    public void updateWords(List<WordBuild> newWords) {
        this.words.clear();
        this.words.addAll(newWords);
        Log.d("WordAdapterBuild", "Updated words: " + newWords.size());
        notifyDataSetChanged();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            if (wordTextView == null) {
                Log.e("WordAdapterBuild", "wordTextView is null in item_word layout");
            }
        }
    }

    interface OnWordClickListener {
        void onWordClick(WordBuild word);
    }
}