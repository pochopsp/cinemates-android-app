package it.unina.cinemates.ui.user.profile.logged.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.cloudservices.cognito.deleteaccount.DeleteAccountHandler;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.user.profile.logged.ProfileSettingsViewModel;


public class DeleteAccountFragment extends Fragment {


    private DeleteAccountHandler deleteAccountHandler;

    private ProfileSettingsViewModel profileSettingsViewModel;


    private Button deleteAccountContinueButton;
    private Button deleteAccountYesButton;
    private Button deleteAccountDeleteButton;



    private Snackbar serverUnreachableSnackbar;


    private ConstraintLayout deleteAccountFirstStepLayout;
    private ConstraintLayout deleteAccountSecondStepLayout;
    private ConstraintLayout deleteAccountThirdStepLayout;


    private String typedUsername = "";
    private TextView usernameErrorTextView;
    private TextInputEditText usernameEditText;
    private TextView deleteAccountTitleTextView;
    private ImageView backIcon;
    private boolean deleteAccountSuccess;


    public DeleteAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.deleteAccountHandler = new DeleteAccountHandler((MainActivity) requireActivity());
        profileSettingsViewModel = new ViewModelProvider(requireActivity()).get(ProfileSettingsViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                DeleteAccountFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void goBack() {
        if(!deleteAccountSuccess) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        }else{
            backToLoginAfterDelete();
        }

        profileSettingsViewModel.resetAllData();
    }

    @SuppressLint("RestrictedApi")
    private void backToLoginAfterDelete() {
        NavController navController = Navigation.findNavController(requireView());
        navController.getBackStack().clear();
        Intent loginActivityIntent = new Intent(requireActivity(), LoginActivity.class);
        requireActivity().startActivity(loginActivityIntent);
        requireActivity().finish();
        deleteAccountSuccess = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_user_delete_account, container, false);

        backIcon = view.findViewById(R.id.logged_user_delete_account_back_icon);
        backIcon.setOnClickListener(v -> DeleteAccountFragment.this.goBack());

        deleteAccountTitleTextView = view.findViewById(R.id.logged_user_delete_account_title_text_view);


        deleteAccountFirstStepLayout = view.findViewById(R.id.logged_user_delete_account_first_step_layout);
        deleteAccountSecondStepLayout = view.findViewById(R.id.logged_user_delete_account_second_step_layout);
        deleteAccountThirdStepLayout = view.findViewById(R.id.logged_user_delete_account_third_step_layout);


        deleteAccountContinueButton = view.findViewById(R.id.logged_user_delete_account_continue_button);
        deleteAccountContinueButton.setOnClickListener(v -> {
            deleteAccountFirstStepLayout.setVisibility(View.GONE);
            deleteAccountSecondStepLayout.setVisibility(View.VISIBLE);
        });


        Button cancelDeleteAccountButton = view.findViewById(R.id.logged_user_delete_account_cancel_button);
        cancelDeleteAccountButton.setOnClickListener(v -> DeleteAccountFragment.this.goBack());

        deleteAccountYesButton = view.findViewById(R.id.logged_user_delete_account_yes_button);
        deleteAccountYesButton.setOnClickListener(v -> {
            deleteAccountSecondStepLayout.setVisibility(View.GONE);
            deleteAccountThirdStepLayout.setVisibility(View.VISIBLE);
        });

        usernameErrorTextView= view.findViewById(R.id.logged_user_delete_account_username_not_matching_text_view);

        deleteAccountDeleteButton = view.findViewById(R.id.logged_user_delete_account_delete_account_button);
        deleteAccountDeleteButton.setOnClickListener(v -> {
            if (!typedUsername.equals(LoggedUser.getInstance().getUsername())){
                usernameErrorTextView.setVisibility(View.VISIBLE);
            }else{
                profileSettingsViewModel.requestDeleteAccount(typedUsername, deleteAccountHandler);
            }
        });

        usernameEditText = view.findViewById(R.id.logged_user_delete_account_username_edit_text);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameErrorTextView.setVisibility(View.INVISIBLE);
                typedUsername = s.toString();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });



        profileSettingsViewModel.getDeleteAccountResult().observe(getViewLifecycleOwner(), backendOperationResult -> {

            switch(backendOperationResult){
                case SUCCESS:
                    this.deleteAccountSuccess = true;
                    deleteAccountThirdStepLayout.setVisibility(View.GONE);
                    showSuccessGui(view);
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableSnackbar.show();
                    break;
            }

            profileSettingsViewModel.resetDeleteAccountResult();
        });


        serverUnreachableSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), deleteAccountDeleteButton);

        return view;
    }

    private void showSuccessGui(View view) {
        backIcon.setVisibility(View.INVISIBLE);
        deleteAccountTitleTextView.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.logged_user_delete_account_success_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.logged_user_delete_account_back_to_login_button).setOnClickListener(v -> this.backToLoginAfterDelete());
    }

}