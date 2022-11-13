package it.unina.cinemates.ui.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import it.unina.cinemates.R;

public class SnackbarUtils {

    public static Snackbar successSnackbar(Activity activity, String message) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = setupSnackbar((FragmentActivity) activity, message, snackbar);
        RelativeLayout relativeLayout = customSnackView.findViewById(R.id.snackbar_relative_layout);
        relativeLayout.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.snackbar_success_green));
        return snackbar;

    }

    public static Snackbar failureSnackbar(Activity activity, String message) {

        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = setupSnackbar((FragmentActivity) activity, message, snackbar);
        RelativeLayout relativeLayout = customSnackView.findViewById(R.id.snackbar_relative_layout);
        relativeLayout.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.snackbar_failure_red));
        return snackbar;
    }

    public static Snackbar successAnchorSnackbar(Activity activity, String message, View anchor) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG).setAnchorView(anchor);
        View customSnackView = setupSnackbar((FragmentActivity) activity, message, snackbar);
        RelativeLayout relativeLayout = customSnackView.findViewById(R.id.snackbar_relative_layout);
        relativeLayout.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.snackbar_success_green));
        return snackbar;
    }

    public static Snackbar failureAnchorSnackbar(Activity activity, String message, View anchor) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG).setAnchorView(anchor);
        View customSnackView = setupSnackbar((FragmentActivity) activity, message, snackbar);
        RelativeLayout relativeLayout = customSnackView.findViewById(R.id.snackbar_relative_layout);
        relativeLayout.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.snackbar_failure_red));
        return snackbar;
    }

    @NonNull
    private static View setupSnackbar(FragmentActivity activity, String message, Snackbar snackbar) {
        @SuppressLint("InflateParams") View customSnackView = activity.getLayoutInflater().inflate(R.layout.snackbar_layout, null);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbar.getView().getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        snackbar.getView().setLayoutParams(params);

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(24, 0, 24, 24);

        TextView messageTextView = customSnackView.findViewById(R.id.message_snackbar_textview);
        messageTextView.setText(message);

        ImageView closeIcon = customSnackView.findViewById(R.id.close_snackbar_icon);
        closeIcon.setOnClickListener(v -> snackbar.dismiss());

        ViewCompat.setElevation(snackbar.getView(), 70f);

        snackbarLayout.addView(customSnackView, 0);

        snackbar.getView().setBackground(AppCompatResources.getDrawable(activity, R.drawable.snackbar_background));
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        return customSnackView;
    }

}
