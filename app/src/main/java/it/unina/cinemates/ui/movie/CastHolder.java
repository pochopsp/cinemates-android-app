package it.unina.cinemates.ui.movie;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.cinemates.R;


public class CastHolder extends RecyclerView.ViewHolder {

    private final ImageView imageView;
    private final TextView nameTextView;
    private final TextView characterTextView;

    public CastHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.cast_imageview);
        nameTextView = itemView.findViewById(R.id.cast_name_textview);
        characterTextView = itemView.findViewById(R.id.cast_character_textview);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getNameTextView() {
        return nameTextView;
    }

    public TextView getCharacterTextView() {
        return characterTextView;
    }
}
