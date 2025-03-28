package com.julien.go4lunch.model;

import java.util.Objects;

public class GPSStatus {
    private final Double latitude;
    private final Double longitude;
    private final boolean hasGPSPermission;
    private final boolean isQuerying;

    public GPSStatus(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        hasGPSPermission = true;
        isQuerying = false;
    }

    public GPSStatus(boolean hasGPSPermission, boolean isQuerying) {
        latitude = null;
        longitude = null;
        this.hasGPSPermission = hasGPSPermission;
        this.isQuerying = isQuerying;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isHasGPSPermission() {
        return hasGPSPermission;
    }

    public boolean isQuerying() {
        return isQuerying;
    }

    @Override
    public String toString() {
        return "GPSStatus{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", hasGPSPermission=" + hasGPSPermission +
                ", isQuerying=" + isQuerying +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GPSStatus gpsStatus = (GPSStatus) o;
        return hasGPSPermission == gpsStatus.hasGPSPermission && isQuerying == gpsStatus.isQuerying && Objects.equals(latitude, gpsStatus.latitude) && Objects.equals(longitude, gpsStatus.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, hasGPSPermission, isQuerying);
    }
}
