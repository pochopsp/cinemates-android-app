package it.unina.cinemates.ui.common.utils;

import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.Calendar;

import it.unina.cinemates.R;

public class DatePickerDialogUtils {

    public static MonthPickerDialog yearDialogWithOutputTextView(int minYear, int yearsAfterCurrentYear, int titleStringId, TextView outputTextView, FragmentActivity activity) {
        Calendar calendar = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(activity,
                (selectedMonth, selectedYear) -> outputTextView.setText(String.valueOf(selectedYear)), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        return builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(minYear)
                .setActivatedYear(calendar.get(Calendar.YEAR))
                .setMaxYear(calendar.get(Calendar.YEAR) + yearsAfterCurrentYear)
                .setTitle(activity.getString(titleStringId))
                .showYearOnly()
                .build();
    }

    public static MonthPickerDialog yearDialogWithOutputTextView(int minYear, int yearsAfterCurrentYear, int activatedYear,
                                                                 int titleStringId, TextView outputTextView, FragmentActivity activity) {
        Calendar calendar = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(activity,
                (selectedMonth, selectedYear) -> outputTextView.setText(String.valueOf(selectedYear)), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        return builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(minYear)
                .setActivatedYear(activatedYear)
                .setMaxYear(calendar.get(Calendar.YEAR) + yearsAfterCurrentYear)
                .setTitle(activity.getString(titleStringId))
                .showYearOnly()
                .build();
    }

}
