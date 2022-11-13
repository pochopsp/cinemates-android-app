package it.unina.cinemates.ui.movie;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import it.unina.cinemates.R;

public class MoviesFromActorBottomSheet extends BottomSheetDialogFragment {

    //private static final String TAG = "MOVIESFROMACTOR_BOTTOMSHEET";
    private final View.OnClickListener moviesFromActorClickListener;
    private final String actor;


    public MoviesFromActorBottomSheet(View.OnClickListener moviesFromActorClickListener, String actor) {
        this.moviesFromActorClickListener = moviesFromActorClickListener;
        this.actor = actor;
    }


    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movies_from_actor_bottom_sheet, container, false);

        Button moviesFromActorButton = view.findViewById(R.id.movies_from_actor_button);

        String buttonText = moviesFromActorButton.getText().toString();

        moviesFromActorButton.setText(buttonText + " " + actor);

        moviesFromActorButton.setOnClickListener(moviesFromActorClickListener);

        return view;
    }

}