package com.safety.womensafety;

public class AlertRecord {
    private long alertId;
    private int userId;
    private String timestamp;
    private double latitude;
    private double longitude;
    private String address;
    private String triggerType; // BUTTON or SHAKE
    private String audioPath;
    private String status; // ACTIVE or RESOLVED

    public AlertRecord(long alertId, int userId, String timestamp, double latitude,
                       double longitude, String address, String triggerType,
                       String audioPath, String status) {
        this.alertId = alertId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.triggerType = triggerType;
        this.audioPath = audioPath;
        this.status = status;
    }

    public long getAlertId() { return alertId; }
    public int getUserId() { return userId; }
    public String getTimestamp() { return timestamp; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address != null ? address : "Unknown Location"; }
    public String getTriggerType() { return triggerType != null ? triggerType : "BUTTON"; }
    public String getAudioPath() { return audioPath; }
    public String getStatus() { return status; }
    public boolean isActive() { return "ACTIVE".equals(status); }
}