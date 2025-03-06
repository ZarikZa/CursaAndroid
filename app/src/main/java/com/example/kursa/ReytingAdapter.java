package com.example.kursa;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReytingAdapter extends RecyclerView.Adapter<ReytingAdapter.UserViewHolder> {
    private List<User> users;

    public ReytingAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_use, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nicknameTextView;
        private TextView pointsTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.userName);
            pointsTextView = itemView.findViewById(R.id.userPoints);
        }

        public void bind(User user) {
            nicknameTextView.setText(user.getNickname());
            pointsTextView.setText(String.valueOf(user.getReytingPoints()));
        }
    }
}
