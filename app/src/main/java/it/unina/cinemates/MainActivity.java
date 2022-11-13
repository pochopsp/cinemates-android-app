package it.unina.cinemates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unina.cinemates.cloudservices.cognito.logout.LogoutHandler;
import it.unina.cinemates.viewmodels.HomeViewModel;
import it.unina.cinemates.viewmodels.NotificationViewModel;

public class MainActivity extends AppCompatActivity {

    //private static final String TAG = "MAIN_ACTIVITY";

    private NotificationViewModel notificationViewModel;

    private LogoutHandler logoutHandler;
    public void logout() {
        logoutHandler.logout();
    }


    private InputMethodManager inputMethodManager;

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    private NavController navController;

    private HomeViewModel homeViewModel;

    private ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        notificationViewModel.checkNewNotifications();

        logoutHandler = new LogoutHandler(this);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        //used to hide keyboard in case it does not hide automatically
        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        setContentView(R.layout.activity_main);
        this.bottomNavigationView = findViewById(R.id.nav_view);

        new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_search, R.id.navigation_discover_lists, R.id.navigation_suggest_movie).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(this.bottomNavigationView, navController);

        toolbar = findViewById(R.id.toolbar);

        Button notificationIcon = findViewById(R.id.notification_topbar_icon);
        notificationIcon.setOnClickListener(v -> {
            if(showcaseView != null)
                showcaseView.hide();
            navController.navigate(R.id.navigation_notification);
        });

        Button accountIcon = findViewById(R.id.account_topbar_icon);
        accountIcon.setOnClickListener(v -> {
            if(showcaseView != null)
                showcaseView.hide();
            navController.navigate(R.id.navigation_logged_user_profile);
        });

        TextView newNotificationIndicator = findViewById(R.id.new_notifications_indicator);
        newNotificationIndicator.setVisibility(View.INVISIBLE);

        notificationViewModel.getNewNotificationsLiveData().observe(this,newNotifications ->{
            if(newNotifications)
                newNotificationIndicator.setVisibility(View.VISIBLE);
            else
                newNotificationIndicator.setVisibility(View.INVISIBLE);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("homeTutorial", false)) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("homeTutorial", true);
            editor.apply();

            showcaseView = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(accountIcon))
                    .setContentTitle(getString(R.string.movielists_tutorial_title))
                    .setContentText(R.string.tooltip_user_profile_text)
                    .hideOnTouchOutside()
                    .build();
        }


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.navigation_home:
                case R.id.navigation_search:
                case R.id.navigation_discover_lists:
                case R.id.navigation_suggest_movie:
                    homeViewModel.setBackFromMovieFullOrInsertList(false);
                    showToolbarAndBottomView();
                    notificationViewModel.checkNewNotifications();
                    break;
                case R.id.navigation_view_all_movies:
                    showToolbarAndBottomView();
                    break;
                case R.id.navigation_movies_from_actor:
                case R.id.navigation_logged_user_profile:
                case R.id.navigation_other_user_profile:
                case R.id.navigation_notification:
                case R.id.navigation_report:
                case R.id.navigation_logged_user_other_lists:
                case R.id.navigation_comment:
                case R.id.navigation_create_movie_list:
                case R.id.navigation_add_to_custom_movie_list:
                case R.id.navigation_logged_user_profile_settings:
                case R.id.navigation_logged_user_movie_list:
                case R.id.navigation_other_user_movie_list:
                case R.id.navigation_movie_full:
                    hideToolbarAndBottomView();
                    break;
            }
        });

    }


    private void showToolbarAndBottomView() {
        this.bottomNavigationView.setVisibility(View.VISIBLE);
        this.toolbar.setVisibility(View.VISIBLE);
    }

    private void hideToolbarAndBottomView() {
        this.bottomNavigationView.setVisibility(View.GONE);
        this.toolbar.setVisibility(View.GONE);
    }

/*    private void showOnlyBottomView() {
        this.bottomNavigationView.setVisibility(View.VISIBLE);
        this.toolbar.setVisibility(View.GONE);
    }*/


    public void closeKeyboard(View keyboardRootView) {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {
            inputMethodManager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
            keyboardRootView.requestFocus();
        }
    }

/*    public void openKeyboard(View keyboardRootView) {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {
            inputMethodManager
                    .showSoftInput(view, 0);
            keyboardRootView.requestFocus();
        }
    }*/

    public View getBottomNavigationView() { return bottomNavigationView; }
}