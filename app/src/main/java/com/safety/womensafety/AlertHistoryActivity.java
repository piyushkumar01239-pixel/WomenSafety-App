package com.safety.womensafety;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AlertHistoryActivity extends AppCompatActivity {

    private ListView lvAlerts;
    private TextView tvNoAlerts;
    private Button btnBack;
    private DatabaseHelper dbHelper;
    private int userId;
    private AlertHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        lvAlerts = findViewById(R.id.lvAlerts);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        btnBack = findViewById(R.id.btnHistoryBack);

        btnBack.setOnClickListener(v -> finish());
        loadAlertHistory();
    }

    private void loadAlertHistory() {
        List<AlertRecord> alerts = dbHelper.getAlertHistory(userId);

        if (alerts.isEmpty()) {
            tvNoAlerts.setVisibility(View.VISIBLE);
            lvAlerts.setVisibility(View.GONE);
        } else {
            tvNoAlerts.setVisibility(View.GONE);
            lvAlerts.setVisibility(View.VISIBLE);
            adapter = new AlertHistoryAdapter(this, alerts);
            lvAlerts.setAdapter(adapter);

            // Tap item to open location in maps
            lvAlerts.setOnItemClickListener((parent, view, position, id) -> {
                AlertRecord alert = alerts.get(position);
                if (alert.getLatitude() != 0 && alert.getLongitude() != 0) {
                    String uri = "https://maps.google.com/?q="
                            + alert.getLatitude() + "," + alert.getLongitude();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer to prevent memory leaks
        if (adapter != null) adapter.releasePlayer();
    }
}