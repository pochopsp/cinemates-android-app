package it.unina.cinemates.utils;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static String getRelativeTime(String timestampString, int hourDifference){

        timestampString = timestampString.replace("T"," ");
        String relativeTime;

        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";

        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            Date parsedDate = dateFormat.parse(timestampString);

            assert parsedDate != null;
            relativeTime = DateUtils.getRelativeTimeSpanString(
                    parsedDate.getTime() + (HOUR_IN_MILLIS * hourDifference)  - MINUTE_IN_MILLIS,
                    System.currentTimeMillis(), MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL
            ).toString();

        } catch(Exception e) {
            return "";
        }
        return relativeTime;
    }
}
