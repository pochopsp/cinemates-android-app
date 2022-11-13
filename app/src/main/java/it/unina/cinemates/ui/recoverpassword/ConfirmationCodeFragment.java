package it.unina.cinemates.ui.recoverpassword;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbangbutton.editcodeview.EditCodeView;

import it.unina.cinemates.LoginActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordHandler;
import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordSteps;
import it.unina.cinemates.ui.common.interfaces.InsetsView;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.viewmodels.RecoverPasswordViewModel;

public class ConfirmationCodeFragment extends Fragment implements InsetsView {

    private RecoverPasswordHandler recoverPasswordHandler;
    private RecoverPasswordViewModel recoverPasswordViewModel;

    private String confirmationCode = "";

    public ConfirmationCodeFragment() {
    }

    public static ConfirmationCodeFragment newInstance() {
        return new ConfirmationCodeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverPasswordHandler = ((LoginActivity) requireActivity()).getRecoverPasswordHandler();
        recoverPasswordViewModel = new ViewModelProvider(requireActivity()).get(RecoverPasswordViewModel.class);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_recover_password_confirmation_code, container, false);

        setInsetsView(view);

        TextView confirmationCodeErrorTextView = view.findViewById(R.id.recover_password_confirmation_code_error_text_view);
        confirmationCodeErrorTextView.setVisibility(View.INVISIBLE);

        EditCodeView confirmationCodeEditView = view.findViewById(R.id.recover_password_confirmation_code_edit_text);
        confirmationCodeEditView.setEditCodeWatcher(code -> {
            confirmationCode = code;
            confirmationCodeErrorTextView.setVisibility(View.INVISIBLE);
        });

        recoverPasswordViewModel.getUserRecoverPasswordStep().observe(getViewLifecycleOwner(),userRecoverPasswordSteps -> {
            NavController navController = Navigation.findNavController(view);
            if(userRecoverPasswordSteps == RecoverPasswordSteps.CONFIRMATION_CODE_SUCCESS) {
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
                navController.getBackStack().clear();
                navController.navigate(R.id.nav_new_password);
            }else if(userRecoverPasswordSteps == RecoverPasswordSteps.TOO_MANY_ATTEMPTS){
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
                SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.too_many_attempts)).show();
            }
            else if(userRecoverPasswordSteps == RecoverPasswordSteps.CONFIRMATION_CODE_FAILED){
                confirmationCodeErrorTextView.setVisibility(View.VISIBLE);
                recoverPasswordViewModel.setUserRecoverPasswordSteps(RecoverPasswordSteps.IDLE);
            }
        });


        Button button = view.findViewById(R.id.confirmation_code_continue_button);
        button.setOnClickListener(v -> {
            if(confirmationCode.length() < 6)
                confirmationCodeErrorTextView.setVisibility(View.VISIBLE);
            else
                recoverPasswordHandler.setConfirmationCode(confirmationCode);
        });

        ImageView back_icon = view.findViewById(R.id.back_icon_confirmation_code);
        back_icon.setOnClickListener(v -> {
            if(confirmationCodeErrorTextView.getVisibility() != View.VISIBLE) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            }
        });

        return view;
    }

}