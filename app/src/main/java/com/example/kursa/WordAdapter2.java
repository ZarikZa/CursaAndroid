package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordAdapter2 extends RecyclerView.Adapter<WordAdapter2.ViewHolder> {
    private final List<Word> wordList;

    public WordAdapter2(List<Word> wordList) {
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_for_slovar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.englishWordTextView.setText(word.getEnglish());
        holder.translationTextView.setText(word.getTranslation());
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView englishWordTextView;
        public final TextView translationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            englishWordTextView = itemView.findViewById(R.id.englishWordTextView);
            translationTextView = itemView.findViewById(R.id.translationTextView);
        }
    }
}