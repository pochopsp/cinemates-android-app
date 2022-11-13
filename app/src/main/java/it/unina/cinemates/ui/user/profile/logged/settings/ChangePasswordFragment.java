package it.unina.cinemates.ui.user.profile.logged.settings;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.changepassword.ChangePasswordHandler;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.utils.StringValidation;
import it.unina.cinemates.viewmodels.user.profile.logged.ProfileSettingsViewModel;


public class ChangePasswordFragment extends Fragment {

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmPassword = "";

    private TextInputEditText currentPasswordEditText;

    private TextView currentPasswordErrorTextView;
    private TextView newPasswordErrorTextView;
    private TextView confirmPasswordErrorTextView;

    private ProfileSettingsViewModel profileSettingsViewModel;

    private ChangePasswordHandler changePasswordHandler;
    private Button changePasswordConfirmButton;
    private TextView changePasswordInProgressTextView;
    private Snackbar serverUnreachableSnackbar;
    private Snackbar tooManyAttemptsSnackbar;

    public ChangePasswordFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changePasswordHandler = new ChangePasswordHandler((MainActivity) requireActivity());
        profileSettingsViewModel = new ViewModelProvider(requireActivity()).get(ProfileSettingsViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                ChangePasswordFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goBack() {

        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();

        profileSettingsViewModel.resetAllData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logged_user_change_password, container, false);

        ImageView backIcon = view.findViewById(R.id.logged_user_change_password_back_icon);
        backIcon.setOnClickListener(v -> ChangePasswordFragment.this.goBack());

        changePasswordInProgressTextView = view.findViewById(R.id.logged_user_change_password_in_progress_textview);

        currentPasswordErrorTextView = view.findViewById(R.id.logged_user_change_password_current_password_error_text_view);
        newPasswordErrorTextView = view.findViewById(R.id.logged_user_change_password_policy_error_text_view);
        confirmPasswordErrorTextView = view.findViewById(R.id.logged_user_change_password_confirm_password_error_text_view);


        currentPasswordEditText = view.findViewById(R.id.logged_user_current_password_edit_text);
        TextInputEditText newPasswordEditText = view.findViewById(R.id.logged_user_new_password_edit_text);
        TextInputEditText confirmPasswordEditText = view.findViewById(R.id.logged_user_confirm_password_edit_text);


        currentPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!currentPassword.equals(s.toString())){
                    currentPassword = s.toString();
                    currentPasswordErrorTextView.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!newPassword.equals(s.toString())){
                    newPassword = s.toString();
                    newPasswordErrorTextView.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!confirmPassword.equals(s.toString())){
                    confirmPassword = s.toString();
                    confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        changePasswordConfirmButton = view.findViewById(R.id.logged_user_change_password_confirm_button);
        changePasswordConfirmButton.setOnClickListener(v -> {
            boolean error = false;
            if (Objects.requireNonNull(currentPasswordEditText.getText()).toString().isEmpty() || currentPasswordErrorTextView.getVisibility() == View.VISIBLE){
                currentPasswordErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            if(!StringValidation.isPasswordValid(newPassword)) {
                newPasswordErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            if(!confirmPassword.equals(newPassword)) {
                confirmPasswordErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }

            if(!error) {
                changePasswordInProgressTextView.setVisibility(View.VISIBLE);
                this.profileSettingsViewModel.changePassword(currentPassword, newPassword, changePasswordHandler);
            }
        });


        profileSettingsViewModel.getChangePasswordResult().observe(getViewLifecycleOwner(), backendOperationResult -> {

            changePasswordInProgressTextView.setVisibility(View.INVISIBLE);

            switch(backendOperationResult){
                case SUCCESS:
                    showSuccessGui(view);
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableSnackbar.show();
                    break;
                case BAD_REQUEST:
                    currentPasswordErrorTextView.setVisibility(View.VISIBLE);
                    break;
                case TOO_MANY_ATTEMPTS:
                    tooManyAttemptsSnackbar.show();
                    break;
            }

            profileSettingsViewModel.resetChangePasswordResult();
        });


        serverUnreachableSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), changePasswordConfirmButton);
        tooManyAttemptsSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.too_many_attempts), changePasswordConfirmButton);

        return view;
    }

    private void showSuccessGui(View view) {
        view.findViewById(R.id.logged_user_change_password_subtitle).setVisibility(View.GONE);

        view.findViewById(R.id.logged_user_current_password_text_input_layout).setVisibility(View.GONE);
        currentPasswordErrorTextView.setVisibility(View.GONE);
        view.findViewById(R.id.logged_user_new_password_text_input_layout).setVisibility(View.GONE);
        newPasswordErrorTextView.setVisibility(View.GONE);
        view.findViewById(R.id.logged_user_confirm_password_text_input_layout).setVisibility(View.GONE);
        confirmPasswordErrorTextView.setVisibility(View.GONE);

        changePasswordConfirmButton.setText(R.string.go_back);
        changePasswordConfirmButton.setOnClickListener(v -> ChangePasswordFragment.this.goBack());

        view.findViewById(R.id.logged_user_change_password_success_image_view).setVisibility(View.VISIBLE);
        view.findViewById(R.id.logged_user_change_password_success_text_view).setVisibility(View.VISIBLE);
    }

}