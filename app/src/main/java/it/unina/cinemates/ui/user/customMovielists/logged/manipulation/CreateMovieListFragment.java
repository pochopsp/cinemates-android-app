package it.unina.cinemates.ui.user.customMovielists.logged.manipulation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.retrofit.backend.jsonwrappers.post.MovieListPostJsonWrapper;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class CreateMovieListFragment extends Fragment {

    private static final String TAG = "CREATEMOVIELIST_FRAGMENT";

    private CustomListsViewModel customListsViewModel;

    private Snackbar listNameErrorSnackbar;
    private Snackbar listNameAlreadyTakenErrorSnackbar;
    private Snackbar serverUnreachableErrorSnackbar;

    private TextInputEditText listNameEditText;
    private String currentName = "";

    private Boolean fromBottomSheet;


    public CreateMovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                CreateMovieListFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_movie_list, container, false);
        view.setOnTouchListener((v, event) -> {
            listNameEditText.clearFocus();
            ((MainActivity) requireActivity()).closeKeyboard(view);
            return v.performClick();
        });

        Bundle bundle = getArguments();
        if(bundle != null)
            fromBottomSheet = (Boolean) bundle.get("fromBottomSheet");

        listNameErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_list_name_length_error)).setDuration(3000);
        listNameAlreadyTakenErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.movie_list_name_already_taken)).setDuration(3000);
        serverUnreachableErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable)).setDuration(3000);


        listNameEditText = view.findViewById(R.id.create_new_list_edit_text);
        listNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentName = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        Button cancelButton = view.findViewById(R.id.create_new_list_cancel);
        cancelButton.setOnClickListener(v -> this.goBack());


        customListsViewModel.getCreateMovieListResult().observe(getViewLifecycleOwner(), createMovieListStatus -> {
            switch (createMovieListStatus) {
                case SUCCESS:
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                    break;
                case ALREADY_PRESENT:
                    listNameAlreadyTakenErrorSnackbar.show();
                    customListsViewModel.resetCreateMovieListStatus();
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableErrorSnackbar.show();
                    customListsViewModel.resetCreateMovieListStatus();
                    break;
            }
        });


        Button confirmButton = view.findViewById(R.id.create_new_list_confirm);
        confirmButton.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).closeKeyboard(view);

            currentName = currentName.trim();
            listNameEditText.setText(currentName);

            if (currentName.isEmpty() || currentName.length() > 25)
                listNameErrorSnackbar.show();
            else
                customListsViewModel.requestCreateMovieList(new MovieListPostJsonWrapper(LoggedUser.getInstance().getUsername(), currentName));
        });

        return view;
    }

    private void goBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
        if(fromBottomSheet != null && fromBottomSheet)
            navController.navigateUp();
    }

}
