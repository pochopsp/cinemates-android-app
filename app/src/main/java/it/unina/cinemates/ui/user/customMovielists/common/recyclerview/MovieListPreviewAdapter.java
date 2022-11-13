package it.unina.cinemates.ui.user.customMovielists.common.recyclerview;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.ui.common.interfaces.OnLongClickListenerGenerator;
import it.unina.cinemates.views.backend.MovieListPreview;

public class MovieListPreviewAdapter extends RecyclerView.Adapter<MovieListPreviewHolder> {

    private static final String TAG = "MOVIELISTPREVIEWADAPTER";

    private final OnClickListenerGenerator<MovieListPreview> onClickListenerGenerator;
    private OnLongClickListenerGenerator<MovieListPreview> onLongClickListenerGenerator;

    private List<MovieListPreview> movieLists;

    public MovieListPreviewAdapter(List<MovieListPreview> movieLists, OnClickListenerGenerator<MovieListPreview> onClickListenerGenerator){
        this.onClickListenerGenerator = onClickListenerGenerator;
        this.movieLists = movieLists;
    }

    public MovieListPreviewAdapter(List<MovieListPreview> movieLists,
                                   OnClickListenerGenerator<MovieListPreview> onClickListenerGenerator,
                                   OnLongClickListenerGenerator<MovieListPreview> onLongClickListenerGenerator){
        this.onClickListenerGenerator = onClickListenerGenerator;
        this.onLongClickListenerGenerator = onLongClickListenerGenerator;
        this.movieLists = movieLists;
    }

    @NonNull
    @Override
    public MovieListPreviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movielist_preview_holder,parent,false);
        return new MovieListPreviewHolder(view);
    }

    public void loadMovieLists(List<MovieListPreview> movieLists){
        this.movieLists = movieLists;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieListPreviewHolder holder, int position) {

        MovieListPreview movieList = movieLists.get(position);

        holder.getListNameTextView().setText(movieList.getName());
        holder.getReactionsNumberTextView().setText(String.valueOf(movieList.getReactionsNumber()));
        holder.getCommentsNumberTextView().setText(String.valueOf(movieList.getCommentsNumber()));

        holder.getAverageRatingTextView().setText(movieList.getAverageRating().toString());

        holder.setPosterPaths(movieList.getMoviePosterPaths());

        holder.getView().setOnClickListener(this.onClickListenerGenerator.generate(movieList));

        if(this.onLongClickListenerGenerator != null)
            holder.getView().setOnLongClickListener(this.onLongClickListenerGenerator.generate(movieList));
    }

    @Override
    public int getItemCount() {
        return movieLists.size();
    }

    public void removeList(int deletedListId) {
        int deletedListIndex = IntStream.range(0, movieLists.size())
                .filter(i -> movieLists.get(i).getId() == deletedListId)
                .findFirst().orElse(-1);

        if(deletedListIndex != -1){
            this.movieLists.remove(deletedListIndex);
            this.notifyItemRemoved(deletedListIndex);
        }
    }
}
