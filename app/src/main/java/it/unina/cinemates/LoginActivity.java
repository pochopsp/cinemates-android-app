package it.unina.cinemates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.unina.cinemates.cloudservices.cognito.recoverpassword.RecoverPasswordHandler;

public class LoginActivity extends AppCompatActivity {

    private RecoverPasswordHandler recoverPasswordHandler;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        recoverPasswordHandler = new RecoverPasswordHandler(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        //used to hide keyboard in case it does not hide automatically
        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public RecoverPasswordHandler getRecoverPasswordHandler() {
        return recoverPasswordHandler;
    }

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
}