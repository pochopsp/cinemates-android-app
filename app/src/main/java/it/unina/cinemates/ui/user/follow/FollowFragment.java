package it.unina.cinemates.ui.user.follow;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.cinemates.R;
import it.unina.cinemates.ui.common.recyclerview.ItemDecorationWithInitialSpace;
import it.unina.cinemates.viewmodels.user.follow.FollowViewModel;
import it.unina.cinemates.views.backend.UserResult;
import it.unina.cinemates.views.backend.enums.FollowPageType;


public class FollowFragment extends Fragment {

    private FollowViewModel followViewModel;

    private TextView titleTextView;
    private RecyclerView recyclerView;
    private LoggedUserFollowingAdapter loggedUserFollowingAdapter;
    private LoggedUserFollowersAdapter loggedUserFollowersAdapter;

    //works for both other user's followers/following
    private OtherUserFollowAdapter otherUserFollowAdapter;

    private String otherUserUsername;
    private FollowPageType followPageType;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private final AtomicBoolean loading = new AtomicBoolean(true);
    private int currentPage = 1;

    public FollowFragment() {
    }

    public static FollowFragment newInstance() {
        return new FollowFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        followViewModel = new ViewModelProvider(requireActivity()).get(FollowViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow, container, false);

        Bundle bundle = getArguments();
        assert bundle != null;
        followPageType = FollowPageType.values()[(int) bundle.get("FollowPageTypeOrdinal")];

        this.otherUserUsername = (String) bundle.get("otherUserUsername");

        setupGui(view);

        followViewModel.clearFollowList();

        switch (followPageType) {
            case MY_FOLLOWERS:
                setupMyFollowers();
                break;
            case MY_FOLLOWING:
                setupMyFollowing();
                break;
            case OTHER_USER_FOLLOWERS:
                setupOtherFollowers();
                break;
            case OTHER_USER_FOLLOWING:
                setupOtherFollowing();
        }

        followViewModel.getDeletedFollowLiveData().observe(getViewLifecycleOwner(), deleted -> currentPage = 1);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupOtherFollowing() {
        titleTextView.setText(getString(R.string.other_user_following_title, otherUserUsername));
        recyclerView.setAdapter(otherUserFollowAdapter);
        currentPage = 1;
        followViewModel.requestFollowing(otherUserUsername,currentPage);
        followViewModel.getFollowLiveData().observe(getViewLifecycleOwner(), userResultList -> {
            loading.set(false);
            if(userResultList != null){
                otherUserFollowAdapter.setUsers(userResultList);
                otherUserFollowAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            followViewModel.requestFollowing(otherUserUsername,++currentPage);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupOtherFollowers() {
        titleTextView.setText(getString(R.string.other_user_followers_title, otherUserUsername));
        recyclerView.setAdapter(otherUserFollowAdapter);
        currentPage = 1;
        followViewModel.requestFollowers(otherUserUsername,currentPage);
        followViewModel.getFollowLiveData().observe(getViewLifecycleOwner(), userResultList -> {
            loading.set(false);
            if(userResultList != null){
                otherUserFollowAdapter.setUsers(userResultList);
                otherUserFollowAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            followViewModel.requestFollowers(otherUserUsername,++currentPage);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupMyFollowing() {
        titleTextView.setText(getString(R.string.my_following_title));
        recyclerView.setAdapter(loggedUserFollowingAdapter);
        currentPage = 1;
        followViewModel.requestMyFollowing(currentPage);
        followViewModel.getFollowLiveData().observe(getViewLifecycleOwner(), userResultList -> {
            loading.set(false);
            if(userResultList != null){
                loggedUserFollowingAdapter.setUsers(userResultList);
                loggedUserFollowingAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            followViewModel.requestMyFollowing(++currentPage);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupMyFollowers() {
        titleTextView.setText(getString(R.string.my_followers_title));
        recyclerView.setAdapter(loggedUserFollowersAdapter);
        currentPage = 1;
        followViewModel.requestMyFollowers(currentPage);
        followViewModel.getFollowLiveData().observe(getViewLifecycleOwner(), userResultList -> {
            loading.set(false);
            if(userResultList != null){
                loggedUserFollowersAdapter.setUsers(userResultList);
                loggedUserFollowersAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            followViewModel.requestMyFollowers(++currentPage);
                        }
                    }
                }
            }
        });
    }

    private void setupGui(View view) {
        titleTextView = view.findViewById(R.id.follow_title_list_name_textview);
        recyclerView = view.findViewById(R.id.follow_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new ItemDecorationWithInitialSpace(30,16));
        recyclerView.setLayoutManager(layoutManager);
        List<UserResult> users = new ArrayList<>();
        loggedUserFollowingAdapter = new LoggedUserFollowingAdapter(users,requireContext(),requireActivity());
        loggedUserFollowersAdapter = new LoggedUserFollowersAdapter(users,requireContext(),requireActivity());
        otherUserFollowAdapter = new OtherUserFollowAdapter(users,otherUserUsername,followPageType,requireContext(),requireActivity());

        ImageView backIconImageView = view.findViewById(R.id.back_icon_follow);
        backIconImageView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        /*Bundle bundle = getArguments();
        if(bundle != null)
            otherUserUsername = (String)bundle.get("otherUsername");*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        followViewModel.clearFollowList();
    }
}