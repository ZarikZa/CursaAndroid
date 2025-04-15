
package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
/**
 * ChatAdapter — адаптер для RecyclerView, отображающий список сообщений в чате.
 * Управляет списком объектов ChatMessage, связывает их с элементами интерфейса,
 * предоставляет методы для добавления новых сообщений и обновления UI.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Создает ViewHolder для элемента чата, раздувая layout из item_chat_message.
     *
     * @param parent   Родительский ViewGroup
     * @param viewType Тип представления
     * @return Новый экземпляр ChatViewHolder
     */
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    /**
     * Связывает данные сообщения с элементами ViewHolder.
     * Устанавливает отправителя и текст сообщения в соответствующие TextView.
     *
     * @param holder   ViewHolder для элемента
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.senderTextView.setText(message.getSender() + ":");
        holder.messageTextView.setText(message.getMessage());
    }

    /**
     * Возвращает общее количество сообщений в списке.
     *
     * @return Размер списка сообщений
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Добавляет новое сообщение в список и уведомляет RecyclerView о вставке нового элемента.
     *
     * @param message Сообщение для добавления
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * ViewHolder для элемента чата, содержащий TextView для отображения отправителя и текста сообщения.
     */
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}