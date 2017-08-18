package com.where.client.util;


import java.util.LinkedHashMap;
import java.util.Map;

import android.content.res.Resources;
import com.where.client.R;
import org.threeten.bp.Duration;


// apparently there is no formatter for Durations, roll my own
public class DurationFormatter {

    private DurationFormatter() {
        // nope
    }

    public static String formatElapsedTime(Duration duration, Resources res) {
        int secondsDiff = (int) duration.getSeconds();
        // consider nanos by rounding half up
        // I know, this is exaggerated...
        if (duration.getNano() >= 500_000_000) {
            ++secondsDiff;
        }

        Map<String, Integer> fields = new LinkedHashMap<>(4);
        fields.put(res.getString(R.string.marker_detail_elapsed_days), secondsDiff / (60 * 60 * 24));
        fields.put(res.getString(R.string.marker_detail_elapsed_hours), (secondsDiff % (60 * 60 * 24)) / (60 * 60));
        fields.put(res.getString(R.string.marker_detail_elapsed_minutes), (secondsDiff % (60 * 60)) / 60);
        fields.put(res.getString(R.string.marker_detail_elapsed_seconds), secondsDiff % 60);
        StringBuilder formatted = joinDurationFields(fields);
        if (formatted.length() == 0) {
            formatted.append("-");
        }

        return formatted.toString();
    }

    public static String formatTimeDifference(Duration duration, Resources res) {
        int secondsDiff = (int) duration.getSeconds();

        String sign;
        if (duration.isNegative()) {
            sign = "- ";
            secondsDiff = -secondsDiff;
        } else {
            sign = "+ ";
        }

        Map<String, Integer> fields = new LinkedHashMap<>(2);
        fields.put(res.getString(R.string.marker_detail_elapsed_hours), secondsDiff / (60 * 60));
        fields.put(res.getString(R.string.marker_detail_elapsed_minutes), (secondsDiff % (60 * 60)) / 60);
        StringBuilder formatted = joinDurationFields(fields);

        if (formatted.length() == 0) {
            formatted.append("-");
        } else {
            formatted.insert(0, sign);
        }

        return formatted.toString();
    }

    private static StringBuilder joinDurationFields(Map<String, Integer> fields) {
        StringBuilder joined = new StringBuilder();
        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            if (entry.getValue() > 0) {
                if (joined.length() > 0) {
                    joined.append(" ");
                }
                joined.append(entry.getValue()).append(" ").append(entry.getKey());
            }
        }

        return joined;
    }
}
