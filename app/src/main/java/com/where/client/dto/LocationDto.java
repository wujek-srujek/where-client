package com.where.client.dto;


import android.os.Parcel;
import android.os.Parcelable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;


@Getter
@Setter
@NoArgsConstructor
public class LocationDto implements Parcelable {

    // format: second.nanosecond
    private Instant timestampUtc;

    private ZoneId timeZone;

    private double latitude;

    private double longitude;

    private Double accuracy;

    private LocationDto(Parcel in) {
        timestampUtc = (Instant) in.readSerializable();
        timeZone = (ZoneId) in.readSerializable();
        latitude = in.readDouble();
        longitude = in.readDouble();
        accuracy = (Double) in.readSerializable();
    }

    public static final Parcelable.Creator<LocationDto> CREATOR = new Parcelable.Creator<LocationDto>() {

        @Override
        public LocationDto createFromParcel(Parcel in) {
            return new LocationDto(in);
        }

        @Override
        public LocationDto[] newArray(int size) {
            return new LocationDto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(timestampUtc);
        parcel.writeSerializable(timeZone);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeSerializable(accuracy);
    }
}
