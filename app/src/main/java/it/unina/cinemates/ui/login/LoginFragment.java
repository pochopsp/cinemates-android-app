package it.unina.cinemates.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.utils.StringValidation;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;

public class LoginFragment extends Fragment implements InsetsView {

    private LoginStatusViewModel loginStatusViewModel;

    private TextView emailErrorTextView;
    private TextView passwordErrorTextView;
    private TextView wrongCredentialsErrorTextView;
    private TextView userNotConfirmedErrorTextView;
    private TextView serverUnreachableTextView;
    private TextView loggingInTextView;


    private TextView newEmailTextView;

    private TextView emailSentTextView;

    private EditText emailEditText;
    private EditText passwordEditText;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginStatusViewModel = new ViewModelProvider(requireActivity()).get(LoginStatusViewModel.class);
        loginStatusViewModel.setUpLoginHandler((LoginActivity) requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = new Bundle();

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        setInsetsView(view);
        view.setOnTouchListener((v, event) -> {
            emailEditText.clearFocus();
            passwordEditText.clearFocus();
            ((LoginActivity) requireActivity()).closeKeyboard(view);
            return v.performClick();
        });

        setupErrorMessagesGui(view);
        setupEditTextsKeyListener(view);
        setupButtonsOnClickListener(view);

        loginStatusViewModel.getUserStatus().observe(getViewLifecycleOwner(), userLoginStatus -> {
            loggingInTextView.setVisibility(View.INVISIBLE);
            switch (userLoginStatus) {
                case LOGGED_IN:
                    Intent mainActivityIntent = new Intent(requireContext(), MainActivity.class);
                    requireActivity().startActivity(mainActivityIntent);
                    requireActivity().finish();
                    break;
                case WRONG_CREDENTIALS:
                    wrongCredentialsErrorTextView.setVisibility(View.VISIBLE);
                    break;
                case ACCOUNT_NOT_CONFIRMED:
                    userNotConfirmedErrorTextView.setVisibility(View.VISIBLE);
                    newEmailTextView.setVisibility(View.VISIBLE);
                    break;
                case SERVER_UNREACHABLE:
                    serverUnreachableTextView.setVisibility(View.VISIBLE);
                    break;

            }
        });


        return view;
    }

    private void setupEditTextsKeyListener(View view) {
        emailEditText = view.findViewById(R.id.login_email_edit_text);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailErrorTextView.setVisibility(View.INVISIBLE);
                wrongCredentialsErrorTextView.setVisibility(View.INVISIBLE);
                userNotConfirmedErrorTextView.setVisibility(View.INVISIBLE);
                newEmailTextView.setVisibility(View.INVISIBLE);
                emailSentTextView.setVisibility(View.INVISIBLE);
                serverUnreachableTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordEditText = view.findViewById(R.id.login_password_edit_text);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty())
                    passwordErrorTextView.setVisibility(View.INVISIBLE);
                wrongCredentialsErrorTextView.setVisibility(View.INVISIBLE);
                userNotConfirmedErrorTextView.setVisibility(View.INVISIBLE);
                newEmailTextView.setVisibility(View.INVISIBLE);
                emailSentTextView.setVisibility(View.INVISIBLE);
                serverUnreachableTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupButtonsOnClickListener(View view) {
        TextView signUpTextButton = view.findViewById(R.id.signup_textbutton);
        signUpTextButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_signup);
        });

        TextView forgotPasswordTextButton = view.findViewById(R.id.forgot_password_button);
        forgotPasswordTextButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_forgot_password);
        });

        newEmailTextView.setOnClickListener(v -> {
            loginStatusViewModel.sendNewConfirmationEmail(emailEditText.getText().toString());
            userNotConfirmedErrorTextView.setVisibility(View.INVISIBLE);
            newEmailTextView.setVisibility(View.INVISIBLE);
            emailSentTextView.setVisibility(View.VISIBLE);
        });

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {

            makeAllErrorsInvisible();

            String email = emailEditText.getText().toString().toLowerCase();
            String password = passwordEditText.getText().toString();

            boolean isEmailError = false;
            boolean isPasswordError = false;

            if (email.isEmpty() || !StringValidation.isEmailValid(email)) {
                emailErrorTextView.setVisibility(View.VISIBLE);
                isEmailError = true;
            }

            if (password.isEmpty()) {
                passwordErrorTextView.setVisibility(View.VISIBLE);
                isPasswordError = true;
            }

            if (!isEmailError && !isPasswordError) {
                loggingInTextView.setVisibility(View.VISIBLE);
                loginStatusViewModel.login(email,password);
            }

        });
    }

    private void setupErrorMessagesGui(View view) {
        emailErrorTextView = view.findViewById(R.id.email_error_text_view);
        passwordErrorTextView = view.findViewById(R.id.password_error_text_view);
        userNotConfirmedErrorTextView = view.findViewById(R.id.user_not_confirmed_error_text_view);
        wrongCredentialsErrorTextView = view.findViewById(R.id.wrong_credentials_error_text_view);
        newEmailTextView = view.findViewById(R.id.new_email_button);
        emailSentTextView = view.findViewById(R.id.email_sent_text_view);
        serverUnreachableTextView = view.findViewById(R.id.server_unreachable_text_view);
        loggingInTextView = view.findViewById(R.id.logging_in_text_view);
    }

    private void makeAllErrorsInvisible() {
        emailErrorTextView.setVisibility(View.INVISIBLE);
        passwordErrorTextView.setVisibility(View.INVISIBLE);
        userNotConfirmedErrorTextView.setVisibility(View.INVISIBLE);
        wrongCredentialsErrorTextView.setVisibility(View.INVISIBLE);
        newEmailTextView.setVisibility(View.INVISIBLE);
        emailSentTextView.setVisibility(View.INVISIBLE);
        serverUnreachableTextView.setVisibility(View.INVISIBLE);
    }
}