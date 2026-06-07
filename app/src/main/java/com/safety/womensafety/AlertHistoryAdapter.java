package com.safety.womensafety;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;

public class AlertHistoryAdapter extends ArrayAdapter<AlertRecord> {

    private final Context context;
    private final List<AlertRecord> alerts;
    private MediaPlayer currentPlayer = null;
    private Button currentStopBtn = null;
    private Button currentPlayBtn = null;

    public AlertHistoryAdapter(Context context, List<AlertRecord> alerts) {
        super(context, R.layout.item_alert_history, alerts);
        this.context = context;
        this.alerts = alerts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_alert_history, parent, false);
        }

        AlertRecord alert = alerts.get(position);

        TextView tvTimestamp = convertView.findViewById(R.id.tvAlertTimestamp);
        TextView tvLocation = convertView.findViewById(R.id.tvAlertLocation);
        TextView tvTrigger = convertView.findViewById(R.id.tvAlertTrigger);
        TextView tvStatus = convertView.findViewById(R.id.tvAlertStatus);
        TextView tvAudio = convertView.findViewById(R.id.tvAlertAudio);
        LinearLayout layoutAudio = convertView.findViewById(R.id.layoutAudio);
        Button btnPlay = convertView.findViewById(R.id.btnPlayAudio);
        Button btnStop = convertView.findViewById(R.id.btnStopAudio);

        // Set basic info
        tvTimestamp.setText("🕐 " + alert.getTimestamp());
        tvLocation.setText("📍 " + alert.getAddress());
        String triggerIcon = "SHAKE".equals(alert.getTriggerType()) ? "📳 Shake" : "🆘 Button";
        tvTrigger.setText("Triggered by: " + triggerIcon);

        if (alert.isActive()) {
            tvStatus.setText("⚡ ACTIVE");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvStatus.setText("✅ Resolved");
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        // Audio section
        String audioPath = alert.getAudioPath();
        if (!TextUtils.isEmpty(audioPath)) {
            layoutAudio.setVisibility(View.VISIBLE);

            // Show friendly path
            String displayName = audioPath.contains("/")
                    ? audioPath.substring(audioPath.lastIndexOf("/") + 1)
                    : audioPath;
            tvAudio.setText("🎙️ " + displayName);

            // Reset button states
            btnPlay.setEnabled(true);
            btnStop.setEnabled(false);

            btnPlay.setOnClickListener(v -> playAudio(audioPath, btnPlay, btnStop));
            btnStop.setOnClickListener(v -> stopAudio(btnPlay, btnStop));

        } else {
            layoutAudio.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void playAudio(String audioPath, Button playBtn, Button stopBtn) {
        // Stop any currently playing audio first
        stopCurrentPlayer();

        try {
            // Try to find the file in Music/SOS_Recordings folder
            File audioFile = resolveAudioFile(audioPath);

            if (audioFile == null || !audioFile.exists()) {
                Toast.makeText(context,
                        "Audio file not found.\nCheck Music/SOS_Recordings folder.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            currentPlayer = new MediaPlayer();
            currentPlayer.setDataSource(audioFile.getAbsolutePath());
            currentPlayer.prepare();
            currentPlayer.start();

            // Update button states
            playBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            currentPlayBtn = playBtn;
            currentStopBtn = stopBtn;

            Toast.makeText(context, "▶ Playing audio...", Toast.LENGTH_SHORT).show();

            // Auto-reset when playback finishes
            currentPlayer.setOnCompletionListener(mp -> {
                stopAudio(playBtn, stopBtn);
                Toast.makeText(context, "⏹ Playback finished", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(context, "Cannot play audio: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            stopAudio(playBtn, stopBtn);
        }
    }

    private void stopAudio(Button playBtn, Button stopBtn) {
        stopCurrentPlayer();
        playBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        currentPlayBtn = null;
        currentStopBtn = null;
    }

    private void stopCurrentPlayer() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) currentPlayer.stop();
                currentPlayer.release();
            } catch (Exception ignored) {}
            currentPlayer = null;
        }
        // Reset previous buttons
        if (currentPlayBtn != null) currentPlayBtn.setEnabled(true);
        if (currentStopBtn != null) currentStopBtn.setEnabled(false);
    }

    /**
     * Tries to find the audio file in multiple locations:
     * 1. Exact path stored in DB
     * 2. Music/SOS_Recordings/ folder
     * 3. Internal app storage
     */
    private File resolveAudioFile(String audioPath) {
        if (audioPath == null || audioPath.isEmpty()) return null;

        // 1. Try exact path first
        File f = new File(audioPath);
        if (f.exists()) return f;

        // 2. Try Music/SOS_Recordings (Android 10+)
        String fileName = audioPath.contains("/")
                ? audioPath.substring(audioPath.lastIndexOf("/") + 1)
                : audioPath;

        File musicDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "SOS_Recordings");
        File musicFile = new File(musicDir, fileName);
        if (musicFile.exists()) return musicFile;

        // 3. Try internal app storage
        File internalFile = new File(
                new File(context.getFilesDir(), "SOS_Recordings"), fileName);
        if (internalFile.exists()) return internalFile;

        return null;
    }

    // Call this when Activity is destroyed to prevent memory leaks
    public void releasePlayer() {
        stopCurrentPlayer();
    }
}