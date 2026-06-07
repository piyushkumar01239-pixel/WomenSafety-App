package com.safety.womensafety;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
    };

    // ⚠️ 100/108/112 do NOT accept SMS in India — we CALL them instead
    private static final String[] EMERGENCY_CALL_NUMBERS = {"112", "100", "108"};
    private static final String[] EMERGENCY_CALL_LABELS = {
            "112 - National Emergency",
            "100 - Police",
            "108 - Ambulance"
    };

    private Button btnSOS, btnContacts, btnProfile, btnHistory, btnSafe, btnHospitals;
    private TextView tvWelcome, tvStatus, tvCountdown;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String userName;

    private LocationManager locationManager;
    private double currentLatitude = 0, currentLongitude = 0;
    private String currentAddress = "Locating...";

    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private Sensor accelerometer;

    private AudioRecorderHelper audioRecorder;
    private String lastAudioPath = null;

    private boolean sosActive = false;
    private long activeAlertId = -1;
    private CountDownTimer sosCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        userName = sharedPreferences.getString("user_name", "User");

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        requestPermissions();
        setupLocation();
        setupShakeDetection();
        setupButtons();

        audioRecorder = new AudioRecorderHelper(this);
        audioRecorder.setOnRecordingCompleteListener(new AudioRecorderHelper.OnRecordingCompleteListener() {
            @Override
            public void onRecordingComplete(String filePath) {
                lastAudioPath = filePath;
                // Update alert record with final audio path
                if (activeAlertId != -1 && filePath != null) {
                    dbHelper.updateAlertAudioPath(activeAlertId, filePath);
                }
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "🎙️ Audio recorded: " + filePath, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onRecordingFailed(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "⚠️ Audio recording failed: " + error, Toast.LENGTH_SHORT).show());
            }
        });

        tvWelcome.setText("Hello, " + userName + " 👋");
    }

    private void initViews() {
        btnSOS = findViewById(R.id.btnSOS);
        btnContacts = findViewById(R.id.btnEmergencyContacts);
        btnProfile = findViewById(R.id.btnProfile);
        btnHistory = findViewById(R.id.btnAlertHistory);
        btnSafe = findViewById(R.id.btnImSafe);
        btnHospitals = findViewById(R.id.btnNearbyHospitals);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvLocationStatus);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnSafe.setVisibility(View.GONE);
        tvCountdown.setVisibility(View.GONE);
    }

    private void setupButtons() {
        btnSOS.setOnClickListener(v -> { if (!sosActive) showSOSConfirmation(); });
        btnSafe.setOnClickListener(v -> cancelSOS());
        btnContacts.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyContactsActivity.class)));
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, UserProfileActivity.class)));
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, AlertHistoryActivity.class)));
        btnHospitals.setOnClickListener(v -> showNearbyHospitals());
    }

    // ==================== SOS LOGIC ====================

    private void showSOSConfirmation() {
        tvCountdown.setVisibility(View.VISIBLE);
        btnSOS.setEnabled(false);

        sosCountdown = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("Sending SOS in " + (millisUntilFinished / 1000)
                        + "s... (Tap I'm Safe to cancel)");
            }
            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                triggerSOS("BUTTON");
            }
        }.start();

        btnSafe.setVisibility(View.VISIBLE);
    }

    private void triggerSOS(String triggerType) {
        sosActive = true;
        btnSOS.setText("🚨 SOS ACTIVE");
        btnSOS.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        btnSafe.setVisibility(View.VISIBLE);

        // 1. Start audio recording
        lastAudioPath = null;
        if (checkPermission(Manifest.permission.RECORD_AUDIO)) {
            lastAudioPath = audioRecorder.startRecording();
        }

        // 2. Save alert to DB
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        activeAlertId = dbHelper.saveAlert(userId, timestamp, currentLatitude,
                currentLongitude, currentAddress, triggerType, lastAudioPath);

        // 3. Send SMS to personal contacts
        sendSOSSmsList();

        // 4. Send email to all contacts that have email
        sendSOSEmailsToContacts();

        // 5. Show emergency call dialog (112/100/108 don't accept SMS)
        showEmergencyCallDialog();

        Toast.makeText(this, "🚨 SOS SENT! SMS + Email dispatched!", Toast.LENGTH_LONG).show();
    }

    private void cancelSOS() {
        if (sosCountdown != null) sosCountdown.cancel();

        sosActive = false;
        btnSOS.setText("🆘 SOS");
        btnSOS.setEnabled(true);
        btnSOS.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        btnSafe.setVisibility(View.GONE);
        tvCountdown.setVisibility(View.GONE);

        audioRecorder.stopRecording();

        if (activeAlertId != -1) {
            dbHelper.resolveAlert(activeAlertId);
            sendImSafeSms();
            activeAlertId = -1;
        }

        Toast.makeText(this, "✅ You're marked as Safe!", Toast.LENGTH_SHORT).show();
    }

    // ==================== SMS ====================

    private void sendSOSSmsList() {
        if (!checkPermission(Manifest.permission.SEND_SMS)) {
            Toast.makeText(this, "SMS permission required!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Contact> contacts = dbHelper.getEmergencyContacts(userId);
        if (contacts.isEmpty()) {
            Toast.makeText(this, "⚠️ No emergency contacts set!", Toast.LENGTH_LONG).show();
            return;
        }

        String mapsLink = "https://maps.google.com/?q=" + currentLatitude + "," + currentLongitude;
        String message = "🚨 SOS from " + userName + "!\n"
                + "📍 " + currentAddress + "\n"
                + "🗺 " + mapsLink + "\n"
                + "Please help immediately!";

        SmsManager sms = SmsManager.getDefault();
        for (Contact c : contacts) {
            try {
                List<String> parts = sms.divideMessage(message);
                sms.sendMultipartTextMessage(c.getPhone(), null,
                        (java.util.ArrayList<String>) parts, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "SMS failed to " + c.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImSafeSms() {
        if (!checkPermission(Manifest.permission.SEND_SMS)) return;
        String msg = "✅ " + userName + " is SAFE now. False alarm. No action needed.";
        SmsManager sms = SmsManager.getDefault();
        for (Contact c : dbHelper.getEmergencyContacts(userId)) {
            try { sms.sendTextMessage(c.getPhone(), null, msg, null, null); }
            catch (Exception ignored) {}
        }
    }

    // ==================== EMAIL ====================

    private void sendSOSEmailsToContacts() {
        List<Contact> contacts = dbHelper.getEmergencyContacts(userId);
        User user = dbHelper.getUserById(userId);
        String userEmail = user != null ? user.getEmail() : "";

        // Count contacts with email
        long emailCount = contacts.stream().filter(Contact::hasEmail).count();

        if (emailCount == 0 && (userEmail == null || userEmail.isEmpty())) {
            // No emails to send — skip silently
            return;
        }

        EmailNotificationHelper.sendSOSToAllContacts(
                contacts, userEmail, userName,
                currentLatitude, currentLongitude,
                currentAddress, lastAudioPath,
                new EmailNotificationHelper.EmailCallback() {
                    @Override
                    public void onSuccess(int sentCount) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this,
                                        "📧 Email sent to " + sentCount + " recipient(s)!",
                                        Toast.LENGTH_SHORT).show());
                    }
                    @Override
                    public void onFailure(String error) {
                        // Email is secondary — don't bother user with failure
                    }
                }
        );
    }

    // ==================== EMERGENCY CALL DIALOG ====================

    /**
     * 100/108/112 do NOT reliably accept SMS in India.
     * We show a dialog so user can CALL them directly.
     */
    private void showEmergencyCallDialog() {
        new AlertDialog.Builder(this)
                .setTitle("📞 Call Emergency Services?")
                .setMessage("SOS sent to your contacts!\n\nDo you also want to call emergency services?\n\n"
                        + "🚓 100 - Police\n"
                        + "🚑 108 - Ambulance\n"
                        + "🆘 112 - National Emergency")
                .setPositiveButton("Call 112 Now", (dialog, which) -> callNumber("112"))
                .setNeutralButton("Call 100 Police", (dialog, which) -> callNumber("100"))
                .setNegativeButton("Skip", null)
                .show();
    }

    private void callNumber(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            startActivity(callIntent);
        } else {
            // Fallback: open dialer with number pre-filled
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + number));
            startActivity(dialIntent);
        }
    }

    // ==================== LOCATION ====================

    private void setupLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return;
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last == null) last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (last != null) updateLocation(last);
        } catch (SecurityException e) {
            tvStatus.setText("Location permission needed");
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override public void onLocationChanged(Location l) { updateLocation(l); }
        @Override public void onStatusChanged(String p, int s, Bundle e) {}
        @Override public void onProviderEnabled(String p) {}
        @Override public void onProviderDisabled(String p) {}
    };

    private void updateLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        new Thread(() -> {
            try {
                Geocoder g = new Geocoder(this, Locale.getDefault());
                List<Address> addrs = g.getFromLocation(currentLatitude, currentLongitude, 1);
                if (addrs != null && !addrs.isEmpty()) {
                    currentAddress = addrs.get(0).getAddressLine(0);
                    runOnUiThread(() -> tvStatus.setText("📍 " + currentAddress));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("📍 " + currentLatitude + ", " + currentLongitude));
            }
        }).start();
    }

    // ==================== SHAKE ====================

    private void setupShakeDetection() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(count -> {
            if (!sosActive) {
                Toast.makeText(this, "📳 Shake detected! Sending SOS...", Toast.LENGTH_SHORT).show();
                triggerSOS("SHAKE");
            }
        });
    }

    // ==================== HOSPITALS ====================

    private void showNearbyHospitals() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🏥 Nearby Hospitals").setMessage("Finding hospitals...").create();
        dialog.show();
        NearbyHospitalHelper.findNearbyHospitals(currentLatitude, currentLongitude,
                new NearbyHospitalHelper.OnHospitalsFoundListener() {
                    @Override public void onHospitalsFound(List<NearbyHospitalHelper.Hospital> hospitals) {
                        dialog.dismiss();
                        runOnUiThread(() -> {
                            if (hospitals.isEmpty()) NearbyHospitalHelper.openNearbyHospitalsInMaps(MainActivity.this, currentLatitude, currentLongitude);
                            else showHospitalList(hospitals);
                        });
                    }
                    @Override public void onError(String error) {
                        dialog.dismiss();
                        runOnUiThread(() -> NearbyHospitalHelper.openNearbyHospitalsInMaps(MainActivity.this, currentLatitude, currentLongitude));
                    }
                });
    }

    private void showHospitalList(List<NearbyHospitalHelper.Hospital> hospitals) {
        String[] names = new String[hospitals.size()];
        for (int i = 0; i < hospitals.size(); i++)
            names[i] = hospitals.get(i).name + "\n" + hospitals.get(i).vicinity;
        new AlertDialog.Builder(this).setTitle("🏥 Nearby Hospitals")
                .setItems(names, (d, which) -> {
                    NearbyHospitalHelper.Hospital h = hospitals.get(which);
                    NearbyHospitalHelper.getDirectionsToHospital(this, currentLatitude, currentLongitude, h.latitude, h.longitude);
                }).setNegativeButton("Cancel", null).show();
    }

    // ==================== PERMISSIONS ====================

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) setupLocation();
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && shakeDetector != null && accelerometer != null)
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        userName = sharedPreferences.getString("user_name", userName);
        tvWelcome.setText("Hello, " + userName + " 👋");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null)
            sensorManager.unregisterListener(shakeDetector);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
        if (audioRecorder != null) audioRecorder.release();
    }
}