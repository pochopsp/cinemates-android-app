package it.unina.cinemates.ui.movie;

import android.content.Context;
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
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.retrofit.tmdb.jsonwrappers.movie.details.CastMember;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.ui.common.utils.GlideUtils;

public class CastAdapter extends RecyclerView.Adapter<CastHolder> {

    private List<CastMember> castMembers;
    private final Context context;

    private final OnClickListenerGenerator<String> onHolderClicked;

    public void setCastMembers(List<CastMember> castMembers) {
        this.castMembers = castMembers;
    }

    public CastAdapter(List<CastMember> castMembers, Context context, OnClickListenerGenerator<String> onHolderClicked) {
        this.castMembers = castMembers;
        this.context = context;
        this.onHolderClicked = onHolderClicked;
    }

    @NonNull
    @Override
    public CastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.cast_holder, parent, false);
        return new CastHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastHolder holder, int position) {

        CastMember castMember = castMembers.get(position);

        if (castMember.getImagePath() != null)
            GlideUtils.loadAndSetImage(TmDbConstants.Images.mediumProfilePath(castMember.getImagePath()), holder.getImageView(), context);
        else
            holder.getImageView().setImageResource(R.drawable.no_cast_photo_placeholder);

        holder.getNameTextView().setText(castMember.getName());
        holder.getCharacterTextView().setText(castMember.getCharacter());

        holder.itemView.setOnClickListener(v -> onHolderClicked.generate(castMember.getName()).onClick(holder.itemView));

    }

    @Override
    public int getItemCount() {
        return castMembers.size();
    }
}
