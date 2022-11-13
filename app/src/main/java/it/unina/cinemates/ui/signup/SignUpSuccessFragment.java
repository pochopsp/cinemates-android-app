package it.unina.cinemates.ui.signup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.signup.SignUpStatus;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.viewmodels.SignUpViewModel;

public class SignUpSuccessFragment extends Fragment implements InsetsView {


    public SignUpSuccessFragment() {
    }

    public static SignUpSuccessFragment newInstance() {
        return new SignUpSuccessFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                SignUpSuccessFragment.this.onBackPressed();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_signup_success, container, false);
        setInsetsView(view);

        Button backToLoginButton = view.findViewById(R.id.signup_back_to_login_button);
        backToLoginButton.setOnClickListener(v -> onBackPressed());

        return view;
    }

}