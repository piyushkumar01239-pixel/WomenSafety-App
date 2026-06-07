package com.safety.womensafety;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WomenSafetyDB";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PHONE = "phone";
    private static final String COL_PASSWORD = "password";
    private static final String COL_BLOOD_GROUP = "blood_group";
    private static final String COL_MEDICAL_INFO = "medical_info";
    private static final String COL_ADDRESS = "address";

    private static final String TABLE_CONTACTS = "emergency_contacts";
    private static final String COL_CONTACT_ID = "contact_id";
    private static final String COL_FK_USER_ID = "user_id";
    private static final String COL_CONTACT_NAME = "contact_name";
    private static final String COL_CONTACT_PHONE = "contact_phone";
    private static final String COL_RELATIONSHIP = "relationship";
    private static final String COL_CONTACT_EMAIL = "contact_email"; // NEW

    private static final String TABLE_ALERTS = "alert_history";
    private static final String COL_ALERT_ID = "alert_id";
    private static final String COL_ALERT_USER_ID = "user_id";
    private static final String COL_ALERT_TIMESTAMP = "timestamp";
    private static final String COL_ALERT_LATITUDE = "latitude";
    private static final String COL_ALERT_LONGITUDE = "longitude";
    private static final String COL_ALERT_ADDRESS = "alert_address";
    private static final String COL_ALERT_TRIGGER = "trigger_type";
    private static final String COL_ALERT_AUDIO_PATH = "audio_path";
    private static final String COL_ALERT_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_PHONE + " TEXT NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_BLOOD_GROUP + " TEXT DEFAULT '', "
                + COL_MEDICAL_INFO + " TEXT DEFAULT '', "
                + COL_ADDRESS + " TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " ("
                + COL_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FK_USER_ID + " INTEGER NOT NULL, "
                + COL_CONTACT_NAME + " TEXT NOT NULL, "
                + COL_CONTACT_PHONE + " TEXT NOT NULL, "
                + COL_RELATIONSHIP + " TEXT DEFAULT '', "
                + COL_CONTACT_EMAIL + " TEXT DEFAULT '', "
                + "FOREIGN KEY(" + COL_FK_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_ALERTS + " ("
                + COL_ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ALERT_USER_ID + " INTEGER NOT NULL, "
                + COL_ALERT_TIMESTAMP + " TEXT NOT NULL, "
                + COL_ALERT_LATITUDE + " REAL DEFAULT 0, "
                + COL_ALERT_LONGITUDE + " REAL DEFAULT 0, "
                + COL_ALERT_ADDRESS + " TEXT DEFAULT '', "
                + COL_ALERT_TRIGGER + " TEXT DEFAULT 'BUTTON', "
                + COL_ALERT_AUDIO_PATH + " TEXT DEFAULT '', "
                + COL_ALERT_STATUS + " TEXT DEFAULT 'ACTIVE')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try { db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_CONTACT_EMAIL + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_BLOOD_GROUP + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_MEDICAL_INFO + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_ADDRESS + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ALERTS + " ("
                        + COL_ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_ALERT_USER_ID + " INTEGER NOT NULL, "
                        + COL_ALERT_TIMESTAMP + " TEXT NOT NULL, "
                        + COL_ALERT_LATITUDE + " REAL DEFAULT 0, "
                        + COL_ALERT_LONGITUDE + " REAL DEFAULT 0, "
                        + COL_ALERT_ADDRESS + " TEXT DEFAULT '', "
                        + COL_ALERT_TRIGGER + " TEXT DEFAULT 'BUTTON', "
                        + COL_ALERT_AUDIO_PATH + " TEXT DEFAULT '', "
                        + COL_ALERT_STATUS + " TEXT DEFAULT 'ACTIVE')");
            } catch (Exception ignored) {}
        }
    }

    // ==================== USER ====================

    public long registerUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_NAME, name); v.put(COL_EMAIL, email);
        v.put(COL_PHONE, phone); v.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, v);
        db.close(); return result;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        User user = null;
        if (c != null && c.moveToFirst()) { user = cursorToUser(c); c.close(); }
        db.close(); return user;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
        boolean exists = c != null && c.getCount() > 0;
        if (c != null) c.close(); db.close(); return exists;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (c != null && c.moveToFirst()) { user = cursorToUser(c); c.close(); }
        db.close(); return user;
    }

    public boolean updateUserProfile(int userId, String name, String phone,
                                     String bloodGroup, String medicalInfo, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_NAME, name); v.put(COL_PHONE, phone);
        v.put(COL_BLOOD_GROUP, bloodGroup); v.put(COL_MEDICAL_INFO, medicalInfo);
        v.put(COL_ADDRESS, address);
        int rows = db.update(TABLE_USERS, v, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close(); return rows > 0;
    }

    private User cursorToUser(Cursor c) {
        int userId = c.getInt(c.getColumnIndexOrThrow(COL_USER_ID));
        String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
        String email = c.getString(c.getColumnIndexOrThrow(COL_EMAIL));
        String phone = c.getString(c.getColumnIndexOrThrow(COL_PHONE));
        String password = c.getString(c.getColumnIndexOrThrow(COL_PASSWORD));
        String bg = "", mi = "", addr = "";
        try { bg = c.getString(c.getColumnIndexOrThrow(COL_BLOOD_GROUP)); } catch (Exception ignored) {}
        try { mi = c.getString(c.getColumnIndexOrThrow(COL_MEDICAL_INFO)); } catch (Exception ignored) {}
        try { addr = c.getString(c.getColumnIndexOrThrow(COL_ADDRESS)); } catch (Exception ignored) {}
        return new User(userId, name, email, phone, password, bg, mi, addr);
    }

    // ==================== CONTACTS ====================

    public long addEmergencyContact(int userId, String name, String phone,
                                    String relationship, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_FK_USER_ID, userId); v.put(COL_CONTACT_NAME, name);
        v.put(COL_CONTACT_PHONE, phone); v.put(COL_RELATIONSHIP, relationship);
        v.put(COL_CONTACT_EMAIL, email != null ? email : "");
        long result = db.insert(TABLE_CONTACTS, null, v);
        db.close(); return result;
    }

    public long addEmergencyContact(int userId, String name, String phone, String relationship) {
        return addEmergencyContact(userId, name, phone, relationship, "");
    }

    public List<Contact> getEmergencyContacts(int userId) {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_CONTACTS, null, COL_FK_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COL_CONTACT_NAME + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(COL_CONTACT_ID));
                String name = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_NAME));
                String phone = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_PHONE));
                String rel = c.getString(c.getColumnIndexOrThrow(COL_RELATIONSHIP));
                String email = "";
                try { email = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_EMAIL)); } catch (Exception ignored) {}
                contacts.add(new Contact(id, userId, name, phone, rel, email));
            }
            c.close();
        }
        db.close(); return contacts;
    }

    public boolean deleteContact(int contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CONTACTS, COL_CONTACT_ID + "=?", new String[]{String.valueOf(contactId)});
        db.close(); return rows > 0;
    }

    // ==================== ALERTS ====================

    public long saveAlert(int userId, String timestamp, double latitude, double longitude,
                          String address, String triggerType, String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ALERT_USER_ID, userId); v.put(COL_ALERT_TIMESTAMP, timestamp);
        v.put(COL_ALERT_LATITUDE, latitude); v.put(COL_ALERT_LONGITUDE, longitude);
        v.put(COL_ALERT_ADDRESS, address); v.put(COL_ALERT_TRIGGER, triggerType);
        v.put(COL_ALERT_AUDIO_PATH, audioPath != null ? audioPath : "");
        v.put(COL_ALERT_STATUS, "ACTIVE");
        long result = db.insert(TABLE_ALERTS, null, v);
        db.close(); return result;
    }

    public boolean resolveAlert(long alertId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ALERT_STATUS, "RESOLVED");
        int rows = db.update(TABLE_ALERTS, v, COL_ALERT_ID + "=?", new String[]{String.valueOf(alertId)});
        db.close(); return rows > 0;
    }

    public List<AlertRecord> getAlertHistory(int userId) {
        List<AlertRecord> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_ALERTS, null, COL_ALERT_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, COL_ALERT_TIMESTAMP + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(COL_ALERT_ID));
                String ts = c.getString(c.getColumnIndexOrThrow(COL_ALERT_TIMESTAMP));
                double lat = c.getDouble(c.getColumnIndexOrThrow(COL_ALERT_LATITUDE));
                double lng = c.getDouble(c.getColumnIndexOrThrow(COL_ALERT_LONGITUDE));
                String addr = c.getString(c.getColumnIndexOrThrow(COL_ALERT_ADDRESS));
                String trigger = c.getString(c.getColumnIndexOrThrow(COL_ALERT_TRIGGER));
                String audio = c.getString(c.getColumnIndexOrThrow(COL_ALERT_AUDIO_PATH));
                String status = c.getString(c.getColumnIndexOrThrow(COL_ALERT_STATUS));
                alerts.add(new AlertRecord(id, userId, ts, lat, lng, addr, trigger, audio, status));
            }
            c.close();
        }
        db.close(); return alerts;
    }

    // Update audio path after recording completes
    public boolean updateAlertAudioPath(long alertId, String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ALERT_AUDIO_PATH, audioPath);
        int rows = db.update(TABLE_ALERTS, v, COL_ALERT_ID + "=?",
                new String[]{String.valueOf(alertId)});
        db.close();
        return rows > 0;
    }
}