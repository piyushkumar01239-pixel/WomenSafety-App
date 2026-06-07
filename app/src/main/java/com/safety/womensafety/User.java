package com.safety.womensafety;

public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String bloodGroup;
    private String medicalInfo;
    private String address;

    // Constructor for basic registration
    public User(int userId, String name, String email, String phone, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.bloodGroup = "";
        this.medicalInfo = "";
        this.address = "";
    }

    // Full constructor with profile fields
    public User(int userId, String name, String email, String phone, String password,
                String bloodGroup, String medicalInfo, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.bloodGroup = bloodGroup;
        this.medicalInfo = medicalInfo;
        this.address = address;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getBloodGroup() { return bloodGroup != null ? bloodGroup : ""; }
    public String getMedicalInfo() { return medicalInfo != null ? medicalInfo : ""; }
    public String getAddress() { return address != null ? address : ""; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setMedicalInfo(String medicalInfo) { this.medicalInfo = medicalInfo; }
    public void setAddress(String address) { this.address = address; }
}