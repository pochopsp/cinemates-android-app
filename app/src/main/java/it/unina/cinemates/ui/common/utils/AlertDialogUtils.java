package it.unina.cinemates.ui.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unina.cinemates.R;

public class AlertDialogUtils {

    //private static final String TAG = "ALERTDIALOG_UTILS";

    public static AlertDialog alertDialog(Context context, int titleStringId, int messageStringId, int positiveButtonStringId, DialogInterface.OnClickListener positiveButtonListener){
        return new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(context.getString(titleStringId))
                .setMessage(context.getString(messageStringId))
                .setPositiveButton(context.getString(positiveButtonStringId), positiveButtonListener)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), (dialog, which) -> {})
                .create();
    }


    public static AlertDialog radioButtonAlertDialogWithNoButtons(Context context, int titleStringId, String[] items, Integer[] selectedItem){
        return new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(context.getString(titleStringId))
                .setSingleChoiceItems(items, selectedItem[0], (dialog, which) -> {
                    selectedItem[0] = which;
                    dialog.dismiss();
                })
                .create();
    }


    public static AlertDialog radioButtonAlertDialog(Context context, String[] items, int titleId, int positiveButtonStringId, DialogInterface.OnClickListener positiveButtonListener, String[] selectedItem){

        final Button[] positiveButton = new Button[1];

        AlertDialog radioButtonDialog = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(context.getString(titleId))
                .setSingleChoiceItems(items, -1, (dialog, which) -> {
                    selectedItem[0] = items[which];
                    if(positiveButton[0] != null)
                        positiveButton[0].setEnabled(true);
                })
                .setPositiveButton(context.getString(positiveButtonStringId), positiveButtonListener)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), (dialog, which) -> {})
                .create();

        radioButtonDialog.setOnShowListener(dialog -> {
            Button button = radioButtonDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setEnabled(false);
            positiveButton[0] = button;
        });


        return radioButtonDialog;
    }

    public static AlertDialog alertDialogWithoutNegativeButton(Context context, int titleStringId, int messageStringId, int positiveButtonStringId){
        return new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(context.getString(titleStringId))
                .setMessage(context.getString(messageStringId))
                .setPositiveButton(context.getString(positiveButtonStringId), (dialog, which) -> {})
                .create();
    }

    public static AlertDialog alertDialogWithTextEdit(Context context, EditText editText, int titleStringId, int positiveButtonStringId){
        @SuppressLint("RestrictedApi") AlertDialog alertDialog = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(titleStringId))
                                    //used to prevent editText to take the same width as the dialog
                .setView(editText, 70, 50, 50, 0)
                .setPositiveButton(context.getString(positiveButtonStringId), null)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), (dialog, which) -> editText.setText(""))
                .create();
        alertDialog
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        return alertDialog;
    }

    public static AlertDialog alertDialogWithBothButtonListeners(Context context, int titleStringId, int messageStringId, int positiveButtonStringId, DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener){
        return new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(context.getString(titleStringId))
                .setMessage(context.getString(messageStringId))
                .setPositiveButton(context.getString(positiveButtonStringId), positiveButtonListener)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), negativeButtonListener)
                .create();
    }
}