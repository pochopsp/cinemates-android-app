package it.unina.cinemates.ui.user.movielist.common.reaction.recyclerview;

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
import it.unina.cinemates.models.Reaction;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionHolder> {

    private List<Reaction> reactions;

    public ReactionAdapter(List<Reaction> reactions){
        this.reactions = reactions;
    }

    @NonNull
    @Override
    public ReactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reaction_holder,parent,false);
        return new ReactionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionHolder holder, int position) {

        Reaction reaction = reactions.get(position);

        holder.getUsernameTextView().setText(reaction.getAuthor().getUsername());
        holder.setUserImage(reaction.getAuthor().getIdPhoto());

        switch (reaction.getType()){
            case Sad:
                holder.getReactionImageView().setImageResource(R.drawable.ic_sad);
                break;
            case Angry:
                holder.getReactionImageView().setImageResource(R.drawable.ic_angry);
                break;
            case Laugh:
                holder.getReactionImageView().setImageResource(R.drawable.ic_laugh);
                break;
            case Like:
                holder.getReactionImageView().setImageResource(R.drawable.ic_like);
                break;
            case Love:
                holder.getReactionImageView().setImageResource(R.drawable.ic_love);
                break;
            case Wow:
                holder.getReactionImageView().setImageResource(R.drawable.ic_wow);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("otherUserUsername", reaction.getAuthor().getUsername());
            NavController navController = Navigation.findNavController(holder.itemView);
            if(reaction.getAuthor().getUsername().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_profile);
            else
                navController.navigate(R.id.navigation_other_user_profile, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return reactions.size();
    }

    public void setReactions(List<Reaction> reactions){
        this.reactions = reactions;
    }
}
