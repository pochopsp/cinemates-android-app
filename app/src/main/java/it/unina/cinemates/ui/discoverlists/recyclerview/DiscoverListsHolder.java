package it.unina.cinemates.ui.discoverlists.recyclerview;

import android.content.res.Resources;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.tmdb.TmDbConstants;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import lombok.Getter;

@Getter
public class DiscoverListsHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "DISCOVERLISTS_HOLDER";

    private View discoverListsHolderView;

    public View getList1View() { return list1View; }

    public View getList2View() {
        return list2View;
    }

    public View getBasicUserView() { return basicUserView; }

    //USER FIELDS
    private final ImageView userPhotoImageView;
    private final TextView usernameTextView;
    private View basicUserView;

    //LIST 1 FIELDS
    private View list1View;
    private List<Pair<ImageView, ImageView>> list1MoviePostersWithOverlays = new ArrayList<>();
    private TextView list1NameTextView;
    private TextView list1ReactionsNumberTextView;
    private TextView list1CommentsNumberTextView;
    private TextView list1AverageRatingTextView;

    //LIST 2 FIELDS
    private View list2View;
    private List<Pair<ImageView, ImageView>> list2MoviePostersWithOverlays = new ArrayList<>();
    private TextView list2NameTextView;
    private TextView list2ReactionsNumberTextView;
    private TextView list2CommentsNumberTextView;
    private TextView list2AverageRatingTextView;

    public DiscoverListsHolder(@NonNull View itemView) {
        super(itemView);

        discoverListsHolderView = itemView;

        userPhotoImageView = itemView.findViewById(R.id.discover_lists_user_image_view);
        usernameTextView = itemView.findViewById(R.id.discover_lists_username_textview);
        basicUserView = itemView.findViewById(R.id.discover_lists_user_layout);

        Resources resources = itemView.getResources();
        setupList1Views(itemView, resources);
        setupList2Views(itemView, resources);
    }

    private void setupList2Views(@NonNull View itemView, Resources resources) {
        list2View = itemView.findViewById(R.id.second_movielist_preview_layout);

        list2NameTextView = itemView.findViewById(R.id.second_list_title_textview);
        list2ReactionsNumberTextView = itemView.findViewById(R.id.second_reactions_number);
        list2CommentsNumberTextView = itemView.findViewById(R.id.second_comments_number);
        list2AverageRatingTextView = itemView.findViewById(R.id.second_avg_rating_value);

        //set all movies poster imageViews for the list2
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("second_" + num + "_movie_poster_overlay", "id", itemView.getContext().getPackageName());
            int posterImageId = resources.getIdentifier("second_" + num + "_movie_poster_image", "id", itemView.getContext().getPackageName());

            ImageView posterImage = itemView.findViewById(posterImageId);
            ImageView posterOverlay = itemView.findViewById(posterOverlayId);

            list2MoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });
    }

    private void setupList1Views(@NonNull View itemView, Resources resources) {
        list1View = itemView.findViewById(R.id.first_movielist_preview_layout);

        list1NameTextView = itemView.findViewById(R.id.first_list_title_textview);
        list1ReactionsNumberTextView = itemView.findViewById(R.id.first_reactions_number);
        list1CommentsNumberTextView = itemView.findViewById(R.id.first_comments_number);
        list1AverageRatingTextView = itemView.findViewById(R.id.first_avg_rating_value);

        //set all movies poster imageViews for the list1
        IntStream.range(1, 5).forEach(num -> {
            int posterOverlayId = resources.getIdentifier("first_" + num + "_movie_poster_overlay", "id", itemView.getContext().getPackageName());
            int posterImageId = resources.getIdentifier("first_" + num + "_movie_poster_image", "id", itemView.getContext().getPackageName());

            ImageView posterImage = itemView.findViewById(posterImageId);
            ImageView posterOverlay = itemView.findViewById(posterOverlayId);

            list1MoviePostersWithOverlays.add(new Pair<>(posterImage, posterOverlay));
        });
    }


    public void setPosterPaths(List<String> list1PosterPaths, List<String> list2PosterPaths) {
        clearImageViews();
        if (list1PosterPaths.size() != 0) {
            ImageView overlay = discoverListsHolderView.findViewById(R.id.first_notempty_list_imageview);
            overlay.setVisibility(View.VISIBLE);
            int i;
            for (i = 0; i < list1PosterPaths.size(); ++i) {
                if(list1PosterPaths.get(i) == null || list1PosterPaths.get(i).equals(""))
                    list1MoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(list1PosterPaths.get(i)), list1MoviePostersWithOverlays.get(i).first, discoverListsHolderView.getContext());
            }
            list1MoviePostersWithOverlays.get(i - 1).second.setVisibility(View.VISIBLE);
        }
        if (list2PosterPaths.size() != 0) {
            ImageView overlay = discoverListsHolderView.findViewById(R.id.second_notempty_list_imageview);
            overlay.setVisibility(View.VISIBLE);
            int i;
            for (i = 0; i < list2PosterPaths.size(); ++i) {
                if(list2PosterPaths.get(i) == null || list2PosterPaths.get(i).equals(""))
                    list2MoviePostersWithOverlays.get(i).first.setImageResource(R.drawable.no_poster_available_movielist_preview);
                else
                    GlideUtils.loadAndSetImage(TmDbConstants.Images.smallPosterPath(list2PosterPaths.get(i)), list2MoviePostersWithOverlays.get(i).first, discoverListsHolderView.getContext());
            }
            list2MoviePostersWithOverlays.get(i - 1).second.setVisibility(View.VISIBLE);
        }
    }

    private void clearImageViews() {
        ImageView overlayList1 = discoverListsHolderView.findViewById(R.id.first_notempty_list_imageview);
        overlayList1.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 4; ++i) {
            list1MoviePostersWithOverlays.get(i).first.setImageDrawable(null);
            list1MoviePostersWithOverlays.get(i).second.setVisibility(View.INVISIBLE);
        }

        ImageView overlayList2 = discoverListsHolderView.findViewById(R.id.second_notempty_list_imageview);
        overlayList2.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 4; ++i) {
            list2MoviePostersWithOverlays.get(i).first.setImageDrawable(null);
            list2MoviePostersWithOverlays.get(i).second.setVisibility(View.INVISIBLE);
        }
    }

}