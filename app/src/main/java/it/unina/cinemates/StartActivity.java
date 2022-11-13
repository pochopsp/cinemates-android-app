package it.unina.cinemates;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import it.unina.cinemates.cloudservices.cognito.login.AutomaticLoginHandler;
import it.unina.cinemates.viewmodels.user.profile.logged.LoginStatusViewModel;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        LoginStatusViewModel loginStatusViewModel = new ViewModelProvider(this).get(LoginStatusViewModel.class);
        loginStatusViewModel.getUserStatus().observe(this, loginStatus -> {
            switch (loginStatus){
                case LOGGED_IN:
                    Intent mainActivityIntent = new Intent(this, MainActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    this.startActivity(mainActivityIntent);
                    this.finish();
                    break;
                case SERVER_UNREACHABLE:
                case LOGGED_OUT:
                    Intent loginActivityIntent = new Intent(this, LoginActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    this.startActivity(loginActivityIntent);
                    this.finish();
                    break;
            }
        });
        AutomaticLoginHandler automaticLoginHandler = new AutomaticLoginHandler(this);
        automaticLoginHandler.automaticLogin();
    }
}
