package it.unina.cinemates.ui.signup;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.signup.SignUpStatus;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.utils.StringValidation;
import it.unina.cinemates.viewmodels.SignUpViewModel;

public class SignUpFragment extends Fragment implements InsetsView {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private SignUpViewModel signUpViewModel;
    private TextView usernameErrorTextView;
    private TextView emailErrorTextView;
    private TextView passwordErrorTextView;
    private TextView confirmPasswordErrorTextView;
    private TextView emailAlreadyTakenErrorTextView;
    private TextView usernameAlreadyTakenErrorTextView;
    private TextView serverUnreachableErrorTextView;
    private TextView genericErrorTextView;
    private TextView signUpInProgressTextView;


    private FloatingActionButton profilePicButton;
    private Uri imageUri = null;


    private String currentPassword = "";
    private String currentConfirmPassword = "";


    public SignUpFragment() {
    }

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpViewModel = new ViewModelProvider(requireActivity()).get(SignUpViewModel.class);
        signUpViewModel.setupSignupHandler((LoginActivity)requireActivity());
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        setInsetsView(view);
        view.setOnTouchListener((v, event) -> {
            emailEditText.clearFocus();
            usernameEditText.clearFocus();
            passwordEditText.clearFocus();
            confirmPasswordEditText.clearFocus();
            ((LoginActivity) requireActivity()).closeKeyboard(view);
            return v.performClick();
        });


        setupErrorTextViews(view);

        signUpViewModel.getSignUpStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case SUCCESS:
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.nav_signup_success);
                    break;
                case EMAIL_TAKEN:
                    emailAlreadyTakenErrorTextView.setVisibility(View.VISIBLE);
                    signUpInProgressTextView.setVisibility(View.INVISIBLE);
                    break;
                case USERNAME_TAKEN:
                    usernameAlreadyTakenErrorTextView.setVisibility(View.VISIBLE);
                    signUpInProgressTextView.setVisibility(View.INVISIBLE);
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableErrorTextView.setVisibility(View.VISIBLE);
                    signUpInProgressTextView.setVisibility(View.INVISIBLE);
                    break;
                case GENERIC_ERROR:
                    genericErrorTextView.setVisibility(View.VISIBLE);
                    signUpInProgressTextView.setVisibility(View.INVISIBLE);
                    break;
            }
        });

        profilePicButton = view.findViewById(R.id.signup_profile_pic_button);
        setupButtonListeners(view);
        setupTextViewListeners(view);

        return view;
    }

    private void setupTextViewListeners(View view) {
        usernameEditText = view.findViewById(R.id.signup_username_edit_text);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameErrorTextView.setVisibility(View.INVISIBLE);
                usernameAlreadyTakenErrorTextView.setVisibility(View.INVISIBLE);
                serverUnreachableErrorTextView.setVisibility(View.INVISIBLE);
                genericErrorTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailEditText = view.findViewById(R.id.signup_email_edit_text);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailErrorTextView.setVisibility(View.INVISIBLE);
                emailAlreadyTakenErrorTextView.setVisibility(View.INVISIBLE);
                serverUnreachableErrorTextView.setVisibility(View.INVISIBLE);
                genericErrorTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordEditText = view.findViewById(R.id.signup_password_edit_text);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //this check is to prevent disappearing of error text views if user presses the eye
                if (!s.toString().equals(currentPassword)) {
                    currentPassword = s.toString();
                    passwordErrorTextView.setVisibility(View.INVISIBLE);
                    confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
                    serverUnreachableErrorTextView.setVisibility(View.INVISIBLE);
                    genericErrorTextView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm_password_edit_text);
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //this check is to prevent disappearing of error text views if user presses the eye
                if (!s.toString().equals(currentConfirmPassword)) {
                    currentConfirmPassword = s.toString();
                    confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
                    passwordErrorTextView.setVisibility(View.INVISIBLE);
                    serverUnreachableErrorTextView.setVisibility(View.INVISIBLE);
                    genericErrorTextView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupButtonListeners(View view) {

        TextView backToLogin = view.findViewById(R.id.login_textbutton);
        backToLogin.setOnClickListener(v -> {
            signUpViewModel.setSignUpStatusLiveData(SignUpStatus.IDLE);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        Button signUpButton = view.findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String email = emailEditText.getText().toString().toLowerCase();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            boolean isError = false;

            if (!StringValidation.isUsernameValid(username)) {
                usernameErrorTextView.setVisibility(View.VISIBLE);
                isError = true;
            }
            if (!StringValidation.isEmailValid(email)) {
                emailErrorTextView.setVisibility(View.VISIBLE);
                isError = true;
            }
            if (!StringValidation.isPasswordValid(password)) {
                passwordErrorTextView.setVisibility(View.VISIBLE);
                isError = true;
            }
            if (!password.equals(confirmPassword)) {
                confirmPasswordErrorTextView.setVisibility(View.VISIBLE);
                isError = true;
            }
            if (serverUnreachableErrorTextView.getVisibility() == View.VISIBLE) {
                isError = true;
            }
            if (!isError) {
                signUpInProgressTextView.setVisibility(View.VISIBLE);
                signUpViewModel.signUp(username, email, password, imageUri);
            }
        });

        ImageView signUpImageView = view.findViewById(R.id.signup_profile_pic_imageview);

        ActivityResultLauncher<String> imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    imageUri = uri;
                    if (uri != null) { //User picked an image
                        GlideUtils.loadAndSetCircleImage(imageUri.toString(), signUpImageView, requireContext());
                        profilePicButton.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_delete_outline_24));
                    }
                });

        profilePicButton.setOnClickListener(v -> {
            if (imageUri != null) {
                profilePicButton.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_add_24));
                signUpImageView.setImageDrawable(null);
                imageUri = null;
            } else
                imagePicker.launch("image/*");
        });
    }

    private void setupErrorTextViews(View view) {
        usernameErrorTextView = view.findViewById(R.id.signup_username_error_text_view);
        usernameErrorTextView.setVisibility(View.INVISIBLE);
        emailErrorTextView = view.findViewById(R.id.signup_email_error_text_view);
        emailErrorTextView.setVisibility(View.INVISIBLE);
        passwordErrorTextView = view.findViewById(R.id.signup_password_error_text_view);
        passwordErrorTextView.setVisibility(View.INVISIBLE);
        confirmPasswordErrorTextView = view.findViewById(R.id.signup_confirm_password_error_text_view);
        confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
        emailAlreadyTakenErrorTextView = view.findViewById(R.id.email_already_taken_error_text_view);
        emailAlreadyTakenErrorTextView.setVisibility(View.INVISIBLE);
        usernameAlreadyTakenErrorTextView = view.findViewById(R.id.signup_username_already_taken_error_text_view);
        usernameAlreadyTakenErrorTextView.setVisibility(View.INVISIBLE);
        serverUnreachableErrorTextView = view.findViewById(R.id.signup_server_unreachable_text_view);
        serverUnreachableErrorTextView.setVisibility(View.INVISIBLE);
        genericErrorTextView = view.findViewById(R.id.signup_generic_error_text_view);
        genericErrorTextView.setVisibility(View.INVISIBLE);
        signUpInProgressTextView = view.findViewById(R.id.signup_in_progress);
        signUpInProgressTextView.setVisibility(View.INVISIBLE);
    }
}