package it.unina.cinemates.ui.user.movielist.common.reaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.user.movielist.common.reaction.recyclerview.ReactionAdapter;
import it.unina.cinemates.viewmodels.user.movielist.common.ReactionViewModel;
import it.unina.cinemates.views.backend.MovieListBasic;
import it.unina.cinemates.models.Reaction;

public class ReactionFragment extends Fragment {

    private ReactionViewModel reactionViewModel;

    private SwipeRefreshLayout refreshLayout;
    private List<Reaction> reactions;
    private ReactionAdapter adapter;

    private TextView listNameTextView;
    private TextView totalReactionNumberTextView;
    private Integer movieListId;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;

    private MovieListBasic movieListBasic;
    private boolean done = false;

    public ReactionFragment() {
    }

    public static ReactionFragment newInstance() {
        return new ReactionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactionViewModel = new ViewModelProvider(requireActivity()).get(ReactionViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reaction, container, false);

        setupGui(view);

        reactionViewModel.getMovieListTotalReactionsLiveData().observe(getViewLifecycleOwner(), totalReactions -> {
            totalReactionNumberTextView.setText(String.format(getString(R.string.total_number_reactions), totalReactions));
            totalReactionNumberTextView.setVisibility(View.VISIBLE);
        });


        Bundle bundle = getArguments();
        assert bundle != null;
        this.movieListBasic = (MovieListBasic) bundle.get("movieList");

        movieListId = movieListBasic.getId();
        String listName = movieListBasic.getName();

        listNameTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle1 = new Bundle();
            bundle1.putInt("movieListId", movieListBasic.getId());
            if(movieListBasic.getOwnerId().equals(LoggedUser.getInstance().getUsername()))
                navController.navigate(R.id.navigation_logged_user_movie_list, bundle1);
            else
                navController.navigate(R.id.navigation_other_user_movie_list, bundle1);
        });

        if (listName == null) {
            switch (movieListBasic.getType()) {
                case ToWatch:
                    listNameTextView.setText(getString(R.string.to_watch_list_title));
                    break;
                case Favorites:
                    listNameTextView.setText(getString(R.string.favorites_list_title));
                    break;
            }
        } else
            listNameTextView.setText(movieListBasic.getName());

        if (!done) {
            done = true;
            reactionViewModel.requestMovieListReactions(movieListId, 1);
        }

        reactionViewModel.getMovieListReactionsLiveData().observe(getViewLifecycleOwner(), allReactions -> {
            if (allReactions == null)
                return;

            reactions = allReactions;
            adapter.setReactions(reactions);
            adapter.notifyDataSetChanged();
        });


        return view;
    }

    private void setupGui(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.reaction_recyclerview);
        refreshLayout = view.findViewById(R.id.reaction_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            reactions.clear();
            currentPage = 1;
            reactionViewModel.requestMovieListReactions(movieListId, 1);
            refreshLayout.setRefreshing(false);
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(38, 16));
        recyclerView.setLayoutManager(layoutManager);
        reactions = new ArrayList<>();
        adapter = new ReactionAdapter(reactions);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            reactionViewModel.requestMovieListReactions(movieListId, ++currentPage);
                        }
                    }
                }
            }
        });

        ImageView backIcon = view.findViewById(R.id.back_icon_reaction);
        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });
        listNameTextView = view.findViewById(R.id.reaction_title_list_name_textview);
        totalReactionNumberTextView = view.findViewById(R.id.total_reactions_textview);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        reactionViewModel.resetReactions();
    }
}