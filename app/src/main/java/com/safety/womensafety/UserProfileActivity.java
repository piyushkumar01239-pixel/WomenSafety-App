package com.safety.womensafety;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etBloodGroup, etMedicalInfo, etAddress;
    private Button btnSaveProfile, btnBack;
    private TextView tvEmail;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        initViews();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etProfileName);
        etPhone = findViewById(R.id.etProfilePhone);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etMedicalInfo = findViewById(R.id.etMedicalInfo);
        etAddress = findViewById(R.id.etAddress);
        tvEmail = findViewById(R.id.tvProfileEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnProfileBack);
    }

    private void loadUserProfile() {
        if (userId == -1) return;
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            etName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone());
            etBloodGroup.setText(user.getBloodGroup());
            etMedicalInfo.setText(user.getMedicalInfo());
            etAddress.setText(user.getAddress());
        }
    }

    private void setupListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String medicalInfo = etMedicalInfo.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }

        boolean updated = dbHelper.updateUserProfile(userId, name, phone, bloodGroup, medicalInfo, address);
        if (updated) {
            // Update session name
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_name", name);
            editor.apply();
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}