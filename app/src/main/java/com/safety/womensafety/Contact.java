package com.safety.womensafety;

public class Contact {
    private int contactId;
    private int userId;
    private String name;
    private String phone;
    private String relationship;
    private String email; // NEW: for email alerts

    public Contact(int contactId, int userId, String name, String phone,
                   String relationship, String email) {
        this.contactId = contactId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
        this.email = email != null ? email : "";
    }

    // Keep old constructor for backward compatibility
    public Contact(int contactId, int userId, String name, String phone, String relationship) {
        this(contactId, userId, name, phone, relationship, "");
    }

    public int getContactId() { return contactId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRelationship() { return relationship; }
    public String getEmail() { return email != null ? email : ""; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public void setEmail(String email) { this.email = email; }

    public boolean hasEmail() { return email != null && !email.isEmpty(); }
}