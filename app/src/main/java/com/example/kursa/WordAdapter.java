package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;



public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private List<Word> wordList;

    public WordAdapter(List<Word> wordList) {
        this.wordList = wordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_for_slovar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.englishWordTextView.setText(word.getEnglish());
        holder.translationTextView.setText(word.getTranslation());
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView englishWordTextView;
        public TextView translationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            englishWordTextView = itemView.findViewById(R.id.englishWordTextView);
            translationTextView = itemView.findViewById(R.id.translationTextView);
        }
    }
}