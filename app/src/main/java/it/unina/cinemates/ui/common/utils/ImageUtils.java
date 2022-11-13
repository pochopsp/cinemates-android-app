package it.unina.cinemates.ui.common.utils;

import android.app.Activity;

public class ImageUtils {

    public static int getDrawableResourceId(Activity activity, String drawableName) {
        return activity.getResources().getIdentifier(drawableName, "drawable", activity.getPackageName());
    }

}
