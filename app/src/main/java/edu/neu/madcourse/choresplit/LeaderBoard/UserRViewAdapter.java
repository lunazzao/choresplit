package edu.neu.madcourse.choresplit.LeaderBoard;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.neu.madcourse.choresplit.R;
import edu.neu.madcourse.choresplit.models.User;

public class UserRViewAdapter extends RecyclerView.Adapter<UserRViewHolder> {
    private final Map<String, User> users;
    private final List<Pair<String, Long>> ranks;

    public UserRViewAdapter(Map<String, User> users, List<Pair<String, Long>> ranks) {
        this.users = users;
        this.ranks = ranks;
    }
    @NonNull
    @Override
    public UserRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_ranking_card,
                parent, false);
        return new UserRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRViewHolder holder, int position) {
        String userId = ranks.get(position).first;
        User user = users.get(userId);


        holder.userName.setText((user == null) ? "Loading..." : user.getName());
        holder.userScore.setText(String.format("#%d",position+1));
    }

    @Override
    public int getItemCount() {
        return ranks.size();
    }
}