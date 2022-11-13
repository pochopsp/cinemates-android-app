package it.unina.cinemates.ui.recoverpassword;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordHandler;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordSteps;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.utils.StringValidation;
import it.unina.cinemates.viewmodels.RecoverPasswordViewModel;

public class NewPasswordFragment extends Fragment implements InsetsView {

    private RecoverPasswordHandler recoverPasswordHandler;
    private RecoverPasswordViewModel recoverPasswordViewModel;


    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    public NewPasswordFragment() { }

    public static NewPasswordFragment newInstance() {
        return new NewPasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        recoverPasswordHandler = ((LoginActivity) requireActivity()).getRecoverPasswordHandler();
        recoverPasswordViewModel = new ViewModelProvider(requireActivity()).get(RecoverPasswordViewModel.class);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                NewPasswordFragment.this.onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @SuppressLint("RestrictedApi")
    private void onBackPressed() {
        Intent loginActivityIntent = new Intent(requireActivity(), LoginActivity.class);
        requireActivity().startActivity(loginActivityIntent);
        requireActivity().finish();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_recover_password_new_password, container, false);
        setInsetsView(view);

        TextView passwordErrorTextView = view.findViewById(R.id.recover_password_password_error_text_view);
        passwordErrorTextView.setVisibility(View.INVISIBLE);
        TextView confirmPasswordErrorTextView = view.findViewById(R.id.recover_password_confirm_password_error_text_view);
        confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);

        passwordEditText = view.findViewById(R.id.new_password_edit_text);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordErrorTextView.setVisibility(View.INVISIBLE);
                confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPasswordEditText = view.findViewById(R.id.confirm_new_password_edit_text);
        confirmPasswordErrorTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordErrorTextView.setVisibility(View.INVISIBLE);
                confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recoverPasswordViewModel.getUserRecoverPasswordStep().observe(getViewLifecycleOwner(),userRecoverPasswordSteps -> {
            if(userRecoverPasswordSteps == RecoverPasswordSteps.CHANGE_PASSWORD_SUCCESS){
                NavController navController = Navigation.findNavController(requireView());
                navController.getBackStack().clear();
                navController.navigate(R.id.nav_recover_password_success);
            }else if(userRecoverPasswordSteps == RecoverPasswordSteps.TOO_MANY_ATTEMPTS){
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.too_many_attempts)).show();
            }
        });

        Button resetPasswordButton = view.findViewById(R.id.recover_password_reset_password_button);
        resetPasswordButton.setOnClickListener(v -> {
            if(!StringValidation.isPasswordValid(passwordEditText.getText().toString()))
                passwordErrorTextView.setVisibility(View.VISIBLE);
            else if(!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()))
                confirmPasswordErrorTextView.setVisibility(View.VISIBLE);
            else
                recoverPasswordHandler.setNewPassword(passwordEditText.getText().toString());
        });

        return view;
    }
}