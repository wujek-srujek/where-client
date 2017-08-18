package com.where.client.activity;


import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.where.client.R;
import com.where.client.dialog.LocationDetailsDialog;
import com.where.client.dialog.MapTypePickerDialog;
import com.where.client.dto.LocationDto;
import com.where.client.gson.InstantTypeAdapter;
import com.where.client.gson.ZoneIdTypeAdapter;
import com.where.client.prefs.Preferences;
import com.where.client.remote.AuthInterceptor;
import com.where.client.remote.WhereService;
import okhttp3.OkHttpClient;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class WhereActivity extends FragmentActivity
        implements OnMapReadyCallback, MapTypePickerDialog.MapTypeCallback {

    private static final String TAG = WhereActivity.class.getSimpleName();

    private WhereService whereService;

    private Geocoder geocoder;

    private Preferences prefs;

    private GoogleMap googleMap;

    private View refreshButton;

    private View mapTypeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_where);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(
                        getString(R.string.server_user),
                        getString(R.string.server_password)))
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .registerTypeAdapter(ZoneId.class, new ZoneIdTypeAdapter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        whereService = retrofit.create(WhereService.class);

        geocoder = new Geocoder(this);

        prefs = new Preferences(this, gson);

        refreshButton = findViewById(R.id.map_button_refresh);
        refreshButton.setEnabled(false);

        mapTypeButton = findViewById(R.id.map_button_map_type);
        mapTypeButton.setEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        mapTypeButton.setEnabled(true);

        refreshLocation();
    }

    @Override
    public int currentMapType() {
        return googleMap.getMapType();
    }

    @Override
    public void onMapTypeChanged(int newType) {
        googleMap.setMapType(newType);
    }

    public void refreshLocation(View view) {
        refreshLocation();
    }

    public void showMapTypeDialog(View view) {
        MapTypePickerDialog.newInstance().show(getFragmentManager(), "mapTypePicker");
    }

    private void refreshLocation() {
        refreshButton.setEnabled(false);

        googleMap.clear();
        whereService.getLatestLocation().enqueue(new Callback<LocationDto>() {

            @Override
            public void onResponse(Call<LocationDto> call, Response<LocationDto> response) {
                if (response.isSuccessful()) {
                    LocationDto locationDto = response.body();
                    prefs.saveLocation(locationDto);
                    showLocation(locationDto);
                } else {
                    String message = "HTTP status code: " + response.code();
                    Log.e(TAG, "error getting latest location: " + message);
                    handleError(message);
                }
            }

            @Override
            public void onFailure(Call<LocationDto> call, Throwable t) {
                Log.e(TAG, "error getting latest location", t);
                handleError(t.toString());
            }
        });
    }

    private void showLocation(final LocationDto locationDto) {
        LatLng location = new LatLng(locationDto.getLatitude(), locationDto.getLongitude());
        String title = getMarkerTitle(locationDto.getLatitude(), locationDto.getLongitude());

        final Marker marker = googleMap.addMarker(new MarkerOptions().position(location).title(title));
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                LocationDetailsDialog.newInstance(locationDto).show(getFragmentManager(), "markerDetails");
            }
        });

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .zoom(4)
                        .tilt(0)
                        .bearing(0)
                        .target(location)
                        .build());
        googleMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                marker.showInfoWindow();

                refreshButton.setEnabled(true);
            }

            @Override
            public void onCancel() {
                // animation cancelled, but still update the state
                marker.showInfoWindow();

                refreshButton.setEnabled(true);
            }
        });
    }

    private String getMarkerTitle(double latitude, double longitude) {
        try {
            // uses Geocoder so should not be run on the main thread, but I don't care in this app
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String country = address.getCountryName();

                StringBuilder title = new StringBuilder();
                for (String s : new String[] { city, country }) {
                    if (!TextUtils.isEmpty(s)) {
                        if (title.length() > 0) {
                            title.append(", ");
                        }
                        title.append(s);
                    }
                }

                return title.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "error getting address from latest location", e);
        }

        return latitude + ", " + longitude;
    }

    private void handleError(String detail) {
        StringBuilder message = new StringBuilder(getString(R.string.message_error_dialog_map));

        // if available, last known location will be shown
        final LocationDto locationDto = prefs.readLocation();
        if (locationDto != null) {
            message.append("\n\n").append(getString(R.string.message_last_location_dialog_map));
        }

        if (detail != null) {
            message.append("\n\n").append(detail);
        }

        new AlertDialog.Builder(WhereActivity.this)
                .setMessage(message)
                .setPositiveButton(
                        getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (locationDto != null) {
                                    showLocation(locationDto);
                                } else {
                                    refreshButton.setEnabled(true);
                                }
                            }
                        })
                .show();
    }
}
