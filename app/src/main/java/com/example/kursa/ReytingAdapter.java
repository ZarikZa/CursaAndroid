package com.example.kursa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
/**
 * ReytingAdapter — адаптер для отображения списка пользователей в рейтинге.
 * Используется в RecyclerView для показа никнейма и рейтинговых баллов каждого пользователя.
 */
public class ReytingAdapter extends RecyclerView.Adapter<ReytingAdapter.UserViewHolder> {
    private List<User> users;

    /**
     * Конструктор адаптера.
     *
     * @param users Список пользователей для отображения
     */
    public ReytingAdapter(List<User> users) {
        this.users = users;
    }

    /**
     * Создает новый ViewHolder для элемента рейтинга.
     *
     * @param parent   Родительская ViewGroup
     * @param viewType Тип представления
     * @return Новый экземпляр UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_use, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Связывает данные пользователя с ViewHolder.
     *
     * @param holder   ViewHolder для элемента
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    /**
     * Возвращает количество пользователей в списке.
     *
     * @return Размер списка пользователей
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder для элемента рейтинга, содержащий поля для никнейма и баллов.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nicknameTextView;
        private TextView pointsTextView;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление элемента
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.userName);
            pointsTextView = itemView.findViewById(R.id.userPoints);
        }

        /**
         * Связывает данные пользователя с элементами интерфейса.
         *
         * @param user Объект пользователя
         */
        public void bind(User user) {
            nicknameTextView.setText(user.getNickname());
            pointsTextView.setText(String.valueOf(user.getReytingPoints()));
        }
    }
}