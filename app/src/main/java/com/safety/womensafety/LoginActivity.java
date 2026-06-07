package com.safety.womensafety;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.loginEmailInput);
        etPassword = findViewById(R.id.loginPasswordInput);
        btnLogin = findViewById(R.id.loginButton);
        tvRegister = findViewById(R.id.signUpLink);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Use loginUser() from new DatabaseHelper which returns a User object
        User user = db.loginUser(email, password);

        if (user != null) {
            // Save session
            SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
            editor.putInt("user_id", user.getUserId());
            editor.putString("user_name", user.getName());
            editor.putString("user_email", user.getEmail());
            editor.apply();

            Toast.makeText(this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
        }
    }
}