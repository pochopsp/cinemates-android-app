package it.unina.cinemates.ui.user.movielist.common.rating.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.views.backend.Rating;

public class RatingAdapter extends RecyclerView.Adapter<RatingHolder> {

    List<Rating> ratings;

    public RatingAdapter(List<Rating> ratings){
        this.ratings = ratings;
    }

    @NonNull
    @Override
    public RatingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rating_holder,parent,false);
        return new RatingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingHolder holder, int position) {

        Rating rating = ratings.get(position);

        holder.getUsernameTextView().setText(rating.getAuthor().getUsername());
        holder.setUserImage(rating.getAuthor().getIdPhoto());
        holder.getRatingBar().setRating(rating.getValue());

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", rating.getAuthor().getUsername());
            NavController navController = Navigation.findNavController(holder.itemView);
            if(rating.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_profile);
            else
                navController.navigate(R.id.navigation_other_user_profile, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    public void setRatingsList(List<Rating> ratings){
        this.ratings = ratings;
    }
}
