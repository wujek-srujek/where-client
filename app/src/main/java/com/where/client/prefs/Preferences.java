package com.where.client.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.where.client.dto.LocationDto;


public class Preferences {

    private static final String PREFS_NAME = "where-prefs";

    private static final String LOCATION_KEY = "LOCATION";

    private final SharedPreferences sharedPreferences;

    private final Gson gson;

    public Preferences(Context context, Gson gson) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = gson;
    }

    public void saveLocation(LocationDto locationDto) {
        sharedPreferences.edit().putString(LOCATION_KEY, gson.toJson(locationDto)).apply();
    }

    public LocationDto readLocation() {
        String locationString = sharedPreferences.getString(LOCATION_KEY, null);

        return locationString != null ? gson.fromJson(locationString, LocationDto.class) : null;
    }
}
