package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private List<WordLevel> wordList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(WordLevel word);
    }

    public WordAdapter(List<WordLevel> wordList, OnDeleteClickListener listener) {
        this.wordList = wordList;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_for_slovar, parent, false);
        return new ViewHolder(view, deleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordLevel word = wordList.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView englishWordTextView;
        private final TextView translationTextView;
        private final ImageButton deleteButton;

        public ViewHolder(View itemView, OnDeleteClickListener listener) {
            super(itemView);
            englishWordTextView = itemView.findViewById(R.id.englishWordTextView);
            translationTextView = itemView.findViewById(R.id.translationTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick((WordLevel) itemView.getTag());
                }
            });
        }

        public void bind(WordLevel word) {
            itemView.setTag(word);
            englishWordTextView.setText(word.getEnglish());
            translationTextView.setText(word.getTranslation());
            deleteButton.setVisibility(word.isHard() ? View.VISIBLE : View.GONE);
        }
    }
}