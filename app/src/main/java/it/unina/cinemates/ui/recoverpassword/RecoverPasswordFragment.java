package it.unina.cinemates.ui.recoverpassword;

import android.os.Bundle;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordHandler;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordSteps;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.utils.StringValidation;
import it.unina.cinemates.viewmodels.RecoverPasswordViewModel;

public class RecoverPasswordFragment extends Fragment implements InsetsView {

    private RecoverPasswordHandler recoverPasswordHandler;
    private RecoverPasswordViewModel recoverPasswordViewModel;

    private EditText emailEditText;

    public RecoverPasswordFragment() {
    }

    public static RecoverPasswordFragment newInstance() {
        return new RecoverPasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverPasswordHandler = ((LoginActivity) requireActivity()).getRecoverPasswordHandler();
        recoverPasswordViewModel = new ViewModelProvider(requireActivity()).get(RecoverPasswordViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recover_password, container, false);
        view.setOnTouchListener((v, event) -> {
            emailEditText.clearFocus();
            ((LoginActivity) requireActivity()).closeKeyboard(view);
            return v.performClick();
        });

        setInsetsView(view);

        TextView emailErrorTextView = view.findViewById(R.id.recover_password_email_error_text_view);
        emailErrorTextView.setVisibility(View.INVISIBLE);

        emailEditText = view.findViewById(R.id.recover_password_email_edit_text);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailErrorTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recoverPasswordViewModel.getUserRecoverPasswordStep().observe(getViewLifecycleOwner(), userRecoverPasswordSteps -> {
            if (userRecoverPasswordSteps == RecoverPasswordSteps.EMAIL_SUCCESS) {
                NavController navController = Navigation.findNavController(requireView());
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
                navController.navigate(R.id.nav_confirmation_code);
            } else if (userRecoverPasswordSteps == RecoverPasswordSteps.TOO_MANY_ATTEMPTS) {
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.too_many_attempts)).show();
            }
        });

        Button continueButton = view.findViewById(R.id.confirmation_code_continue_button);
        continueButton.setOnClickListener(v -> {
            if (!StringValidation.isEmailValid(emailEditText.getText().toString()))
                emailErrorTextView.setVisibility(View.VISIBLE);
            else
                recoverPasswordHandler.setEmailForResetPassword(emailEditText.getText().toString());
        });

        ImageView back_icon = view.findViewById(R.id.recover_password_email_back_icon);
        back_icon.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        return view;
    }
}