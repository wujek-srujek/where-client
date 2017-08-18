package com.where.client.dialog;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.where.client.R;
import com.where.client.dto.LocationDto;
import com.where.client.util.DurationFormatter;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;


public class LocationDetailsDialog extends DialogFragment {

    private static final String LOCATION_KEY = "LOCATION";

    private LocationDto locationDto;

    private DateTimeFormatter dateTimeFormatter;

    private String staticMessagePart;

    private AlertDialog detailsDialog;

    private ScheduledExecutorService refresher;

    public static LocationDetailsDialog newInstance(LocationDto locationDto) {
        Bundle args = new Bundle();
        args.putParcelable(LOCATION_KEY, locationDto);

        LocationDetailsDialog fragment = new LocationDetailsDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationDto = getArguments().getParcelable(LOCATION_KEY);

        dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.marker_detail_date_time_format));
        // create a huge part of the message once, it never changes
        staticMessagePart = createStaticMessagePart();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        detailsDialog = new AlertDialog.Builder(getActivity())
                // there will be a timer to update the text but the text is not
                // shown if the message is not set to non-null at creation time
                // https://issuetracker.google.com/issues/36913966
                .setMessage("")
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();

        return detailsDialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        refresher = Executors.newSingleThreadScheduledExecutor();
        refresher.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (isAdded()) {
                            // Duration will always be positive here
                            String timeElapsed = DurationFormatter.formatElapsedTime(
                                    Duration.between(locationDto.getTimestampUtc(), Instant.now()),
                                    getResources());
                            String savedMessage = getString(R.string.marker_detail_data_saved, timeElapsed);

                            // this should just refresh the 'data saved' part but in this case
                            // it is all a big text due to laziness so refresh all of it ...
                            detailsDialog.setMessage(staticMessagePart + savedMessage);
                        }
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();

        refresher.shutdownNow();
        refresher = null;
    }

    private String createStaticMessagePart() {
        StringBuilder msg = new StringBuilder();
        msg.append(getString(R.string.marker_detail_location)).append("\n");
        msg.append(getString(R.string.marker_detail_latitude, locationDto.getLatitude())).append("\n");
        msg.append(getString(R.string.marker_detail_longitude, locationDto.getLongitude())).append("\n");
        if (locationDto.getAccuracy() != null) {
            msg.append(getString(R.string.marker_detail_accuracy, locationDto.getAccuracy()));
        } else {
            msg.append(getString(R.string.marker_detail_accuracy_no_data));
        }
        msg.append("\n");

        ZonedDateTime dateTime = ZonedDateTime.ofInstant(locationDto.getTimestampUtc(), locationDto.getTimeZone());
        msg.append(getString(R.string.marker_detail_date_time, dateTimeFormatter.format(dateTime))).append("\n");
        msg.append(getString(R.string.marker_detail_time_zone, locationDto.getTimeZone())).append("\n\n");

        msg.append(getString(R.string.marker_detail_phone)).append("\n");
        ZonedDateTime dateTimePhone = dateTime.withZoneSameInstant(ZoneId.systemDefault());
        msg.append(getString(R.string.marker_detail_date_time, dateTimeFormatter.format(dateTimePhone))).append("\n");
        msg.append(getString(R.string.marker_detail_time_zone, ZoneId.systemDefault())).append("\n");

        // calculate time difference between location timezone and current phone time zone
        // by getting a Duration between the same hour at the two time zones
        // may be negative, positive or '-'
        ZonedDateTime sameDateTimePhone = dateTimePhone.withZoneSameLocal(locationDto.getTimeZone());
        String differenceString = DurationFormatter.formatTimeDifference(
                Duration.between(dateTimePhone, sameDateTimePhone),
                getResources());
        msg.append(getString(R.string.marker_detail_time_difference, differenceString)).append("\n\n");

        return msg.toString();
    }
}
