package com.safety.womensafety;

public class Alert {
    private int id;
    private String time;
    private double latitude;
    private double longitude;
    private String status;

    public Alert(int id, String time, double latitude, double longitude, String status) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }
}