package it.unina.cinemates.ui.discoverlists.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.ui.common.interfaces.OnClickListenerGenerator;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.views.backend.BasicUser;
import it.unina.cinemates.views.backend.DiscoverListsElement;
import it.unina.cinemates.views.backend.MovieListPreview;
import it.unina.cinemates.views.backend.enums.ListType;

public class DiscoverListsAdapter extends RecyclerView.Adapter<DiscoverListsHolder> {

    private List<DiscoverListsElement> discoverListsElements;

    private final OnClickListenerGenerator<MovieListPreview> onClickListenerGenerator;
    private final OnClickListenerGenerator<BasicUser> userOnClickListenerGenerator;

    private Context context;

    public DiscoverListsAdapter(List<DiscoverListsElement> discoverListsElements,
                                OnClickListenerGenerator<BasicUser> userOnClickListenerGenerator,
                                OnClickListenerGenerator<MovieListPreview> movieListOnClickListenerGenerator,
                                Context context)
    {
        this.discoverListsElements = discoverListsElements;
        this.userOnClickListenerGenerator = userOnClickListenerGenerator;
        this.onClickListenerGenerator = movieListOnClickListenerGenerator;
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadData(List<DiscoverListsElement> discoverListsElements) {
        this.discoverListsElements = discoverListsElements;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public DiscoverListsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.discover_lists_holder, parent, false);
        return new DiscoverListsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoverListsHolder holder, int position) {

        DiscoverListsElement discoverListsElement = discoverListsElements.get(position);

        holder.getBasicUserView().setOnClickListener(userOnClickListenerGenerator.generate(discoverListsElement.getOwner()));
        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(
                CloudinaryHelper.imagePath(discoverListsElement.getOwner().getIdPhoto()),
                holder.getUserPhotoImageView(),
                context
        );
        holder.getUsernameTextView().setText(discoverListsElement.getOwner().getUsername());


        MovieListPreview list1 = discoverListsElement.getList1();
        MovieListPreview list2 = discoverListsElement.getList2();

        holder.setPosterPaths(list1.getMoviePosterPaths(), list2.getMoviePosterPaths());

        //setting list1
        if(list1.getType() == ListType.ToWatch)
            holder.getList1NameTextView().setText(context.getString(R.string.userprofile_towatch_movielist));
        else if(list1.getType() == ListType.Favorites)
            holder.getList1NameTextView().setText(context.getString(R.string.userprofile_favorites_movielist));
        else
            holder.getList1NameTextView().setText(list1.getName());
        holder.getList1ReactionsNumberTextView().setText(String.valueOf(list1.getReactionsNumber()));
        holder.getList1CommentsNumberTextView().setText(String.valueOf(list1.getCommentsNumber()));
        holder.getList1AverageRatingTextView().setText(list1.getAverageRating().toString());
        holder.getList1View().setOnClickListener(this.onClickListenerGenerator.generate(list1));

        //setting list2
        if(list2.getType() == ListType.ToWatch)
            holder.getList2NameTextView().setText(context.getString(R.string.userprofile_towatch_movielist));
        else if(list2.getType() == ListType.Favorites)
            holder.getList2NameTextView().setText(context.getString(R.string.userprofile_favorites_movielist));
        else
            holder.getList2NameTextView().setText(list2.getName());
        holder.getList2ReactionsNumberTextView().setText(String.valueOf(list2.getReactionsNumber()));
        holder.getList2CommentsNumberTextView().setText(String.valueOf(list2.getCommentsNumber()));
        holder.getList2AverageRatingTextView().setText(list2.getAverageRating().toString());
        holder.getList2View().setOnClickListener(this.onClickListenerGenerator.generate(list2));
    }


    @Override
    public int getItemCount() {
        return discoverListsElements.size();
    }
}
