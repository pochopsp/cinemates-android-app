package it.unina.cinemates;

import android.app.Application;

import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;

public class CineMatesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CloudinaryHelper.config(getApplicationContext());
    }
}
