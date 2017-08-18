package com.where.client.remote;


import com.where.client.dto.LocationDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;


public interface WhereService {

    @GET("where")
    @Headers("Accept: application/json;charset=UTF-8")
    Call<LocationDto> getLatestLocation();
}