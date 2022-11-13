package it.unina.cinemates.ui.discoverlists;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.discoverlists.recyclerview.DiscoverListsAdapter;
import it.unina.cinemates.viewmodels.DiscoverListsViewModel;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;

public class DiscoverListsFragment extends Fragment {

    private static final String TAG = "DISCOVERLISTS_FRAGMENT";

    private static final String[] categories = {"rated", "reacted", "commented"};

    private static final Integer[] selectedCategory = {0};

    DiscoverListsViewModel discoverListsViewModel;

    //RECYCLER VIEW FIELDS
    private DiscoverListsAdapter discoverListsAdapter;
    private RecyclerView discoverListsRecyclerView;


    //RECYCLER VIEW LOGIC FIELDS
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage;

    public DiscoverListsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        discoverListsViewModel = new ViewModelProvider(requireActivity()).get(DiscoverListsViewModel.class);
        //overrideBackPressedBehavior();
    }

/*    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true *//* enabled by default *//*) {
            @Override
            public void handleOnBackPressed() {
                DiscoverListsFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goBack() {

        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();

        discoverListsViewModel.resetDiscoverListsElementsFetch();
    }*/

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_lists, container, false);

        String[] localizedCategories = new String[]{
                getString(R.string.rated_movielist_category),
                getString(R.string.reacted_movielist_category),
                getString(R.string.rated_commented_category)
        };

        Button categoriesButton = view.findViewById(R.id.discover_lists_categories_button);
        categoriesButton.setOnClickListener(
                v ->
                {
                    int previousSelectedCategory = selectedCategory[0];
                    AlertDialog dialog = AlertDialogUtils.radioButtonAlertDialogWithNoButtons(requireContext(), R.string.discover_lists_choose_category_title, localizedCategories, selectedCategory);
                    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                    lp.gravity = Gravity.TOP | Gravity.END;
                    dialog.getWindow().setAttributes(lp);
                    dialog.setOnDismissListener(
                            dialogInterface ->
                            {
                                String previousCategory = categories[previousSelectedCategory];
                                String currentCategory = categories[selectedCategory[0]];
                                if (!currentCategory.equals(previousCategory)) {
                                    resetFetchAndFetch();
                                }
                            });
                    dialog.show();
                }
        );

        setupRecyclerView(view);

        resetFetchAndFetch();

        discoverListsViewModel.getDiscoverListsElements().observe(getViewLifecycleOwner(), discoverLists -> {
            if (discoverLists != null) {
                if (discoverLists.size() > 0) {
                    categoriesButton.setVisibility(View.VISIBLE);
                    discoverListsAdapter.loadData(discoverLists);
                    loading.set(false);
                    discoverListsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        discoverListsViewModel.getServerError().observe(getViewLifecycleOwner(), serverError -> {
            if (serverError == null)
                return;

            View bottomNavigationView = ((MainActivity) requireActivity()).getBottomNavigationView();

            if (serverError) {
                SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), bottomNavigationView).show();
                discoverListsViewModel.resetServerError();
            }
        });

        discoverListsViewModel.getRetrieveResult().observe(getViewLifecycleOwner(), listsRetrieveResult -> {
            if (listsRetrieveResult == CustomListsViewModel.ListsRetrieveResult.NO_LISTS_AT_ALL) {
                ConstraintLayout discoverListsNoElementsLayout = view.findViewById(R.id.discover_lists_no_elements_layout);
                discoverListsNoElementsLayout.setVisibility(View.VISIBLE);
                discoverListsNoElementsLayout.findViewById(R.id.discover_lists_search_users_button).setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("fromDiscoverLists", true);
                    navController.navigate(R.id.navigation_search, bundle);
                });

                discoverListsViewModel.resetDiscoverListsElementsFetch();
            }
        });

        return view;
    }

    private void resetFetchAndFetch() {
        discoverListsViewModel.resetDiscoverListsElementsFetch();
        currentPage = 1;
        discoverListsViewModel.fetchDiscoverListsElements(LoggedUser.getInstance().getUsername(), categories[selectedCategory[0]], currentPage);
    }

    private void setupRecyclerView(View view) {
        discoverListsRecyclerView = view.findViewById(R.id.discover_lists_recycler_view);
        LinearLayoutManager linearLayout = new LinearLayoutManager(requireContext());
        discoverListsRecyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(120, 56));
        discoverListsRecyclerView.setLayoutManager(linearLayout);

        discoverListsAdapter = new DiscoverListsAdapter(
                new ArrayList<>(),
                basicUser -> v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("otherUserUsername", basicUser.getUsername());
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.navigation_other_user_profile, bundle);
                },
                pressedMovieList -> v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bundle = new Bundle();
                    bundle.putInt("movieListId", pressedMovieList.getId());
                    navController.navigate(R.id.navigation_other_user_movie_list, bundle);
                },
                requireContext()
        );
        discoverListsRecyclerView.setAdapter(discoverListsAdapter);

        setupEndlessScrollLogic();
    }


    private void setupEndlessScrollLogic() {

        discoverListsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0) { //check for scroll down
                    assert layoutManager != null;
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            discoverListsViewModel.fetchDiscoverListsElements(LoggedUser.getInstance().getUsername(), categories[selectedCategory[0]], ++currentPage);
                        }
                    }
                }
            }
        });
    }


}