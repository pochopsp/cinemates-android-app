package it.unina.cinemates.ui.notification;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import it.unina.cinemates.models.Notification;
import it.unina.cinemates.retrofit.backend.BackendRetrofitService;
import it.unina.cinemates.ui.notification.recyclerview.NotificationViewAdapter;
import it.unina.cinemates.viewmodels.NotificationViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

     private NotificationViewModel notificationViewModel;

     private ImageView noNotificationImageView;
     private TextView noNotificationTitleTextView;
     private TextView noNotificationTextTextView;
     private Button updateNotificationButton;
     private RecyclerView recyclerView;
     private SwipeRefreshLayout swipeRefreshLayout;
     private ImageView moreOptionIcon;
     private ImageView backIcon;

     private LinearLayoutManager layoutManager;
     private List<Notification> notifications;
     private NotificationViewAdapter notificationViewAdapter;

     private int pastVisibleItems, visibleItemCount, totalItemCount;
     private final AtomicBoolean loading = new AtomicBoolean(true);
     private int currentPage = 1;

     private boolean done = false;

    public NotificationFragment() {
    }


    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        setupGui(view);
        setupListeners(view);
        notificationViewModel.getAllNotificationLiveData().observe(getViewLifecycleOwner(),notifications -> {
            loading.set(false);
            if(notifications != null && notifications.size() != 0){
                notificationViewAdapter.setNotificationList(notifications);
                notificationViewAdapter.notifyDataSetChanged();
            }
        });

        if(!done) {
            notificationViewModel.requestNotification(1);
            currentPage = 1;
            done = true;
        }
        notificationViewModel.getNoNotificationLiveData().observe(getViewLifecycleOwner(),noNotifications -> {
            if(noNotifications == null)
                return;

            if(noNotifications)
                showNoNotificationGui();
            else
                hideNoNotificationGui();

            notificationViewModel.setNoNotificationLiveData(null);
        });

        return view;
    }

    private void setupGui(View view) {

        notifications = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_notification);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        notificationViewAdapter = new NotificationViewAdapter(this.notifications,requireContext(),requireActivity());
        recyclerView.setAdapter(notificationViewAdapter);

        noNotificationImageView = view.findViewById(R.id.no_notification_image_view);
        noNotificationTitleTextView = view.findViewById(R.id.no_notification_title_text_view);
        noNotificationTextTextView = view.findViewById(R.id.no_notification_text_textview);
        updateNotificationButton = view.findViewById(R.id.update_notification_button);
        moreOptionIcon = view.findViewById(R.id.notification_more_option_icon);
        backIcon = view.findViewById(R.id.back_icon_report);
        noNotificationImageView.setVisibility(View.GONE);
        noNotificationTitleTextView.setVisibility(View.GONE);
        noNotificationTextTextView.setVisibility(View.GONE);
        updateNotificationButton.setVisibility(View.GONE);
        moreOptionIcon.setVisibility(View.INVISIBLE);
        hideNoNotificationGui();
    }

    private void setupListeners(View view){
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_view);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            notifications.clear();
            notificationViewModel.resetAllNotifications();
            notificationViewModel.requestNotification(currentPage = 1);
            swipeRefreshLayout.setRefreshing(false);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading.get()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading.set(true);
                            notificationViewModel.requestNotification(++currentPage);
                        }
                    }
                }
            }
        });

        moreOptionIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), moreOptionIcon);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.notification_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                BackendRetrofitService.getInstance().getUserAPI().putAllNotificationRead(LoggedUser.getInstance().getUsername()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        notificationViewModel.resetAllNotifications();
                        notificationViewModel.requestNotification(currentPage = 1);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                    }
                });
                return true;
            });

            popup.show();//showing popup menu
        });

        backIcon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
            notificationViewModel.checkNewNotifications();
        });

        updateNotificationButton.setOnClickListener(v ->
                notificationViewModel.requestNotification(currentPage = 1));
    }

    private void showNoNotificationGui(){
        noNotificationImageView.setVisibility(View.VISIBLE);
        noNotificationTitleTextView.setVisibility(View.VISIBLE);
        noNotificationTextTextView.setVisibility(View.VISIBLE);
        updateNotificationButton.setVisibility(View.VISIBLE);
        moreOptionIcon.setVisibility(View.INVISIBLE);
    }

    private void hideNoNotificationGui(){
        noNotificationImageView.setVisibility(View.GONE);
        noNotificationTitleTextView.setVisibility(View.GONE);
        noNotificationTextTextView.setVisibility(View.GONE);
        updateNotificationButton.setVisibility(View.GONE);
        moreOptionIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetach() {
        notificationViewModel.resetAllNotifications();
        super.onDetach();
    }
}