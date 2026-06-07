package com.safety.womensafety;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecorderHelper {

    private static final String TAG = "AudioRecorder";
    private static final int RECORDING_DURATION_MS = 30000; // 30 seconds

    private MediaRecorder mediaRecorder;
    private String outputFilePath;   // internal temp path during recording
    private String displayPath;      // user-friendly path shown in app
    private boolean isRecording = false;
    private final Context context;
    private OnRecordingCompleteListener listener;

    public interface OnRecordingCompleteListener {
        void onRecordingComplete(String filePath);
        void onRecordingFailed(String error);
    }

    public AudioRecorderHelper(Context context) {
        this.context = context;
    }

    public void setOnRecordingCompleteListener(OnRecordingCompleteListener listener) {
        this.listener = listener;
    }

    public String startRecording() {
        if (isRecording) stopRecording();

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = "SOS_" + timestamp + ".3gp";

            // Save to internal first, then copy to Music folder after recording
            File internalDir = new File(context.getFilesDir(), "SOS_Recordings");
            if (!internalDir.exists()) internalDir.mkdirs();
            outputFilePath = internalDir.getAbsolutePath() + "/" + fileName;
            displayPath = fileName; // will be updated after copy

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(context);
            } else {
                mediaRecorder = new MediaRecorder();
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setAudioEncodingBitRate(12200);
            mediaRecorder.setOutputFile(outputFilePath);
            mediaRecorder.setMaxDuration(RECORDING_DURATION_MS);

            mediaRecorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                    copyToPhoneStorage(outputFilePath, fileName);
                }
            });

            mediaRecorder.setOnErrorListener((mr, what, extra) -> {
                Log.e(TAG, "MediaRecorder error: " + what);
                releaseRecorder();
                if (listener != null) listener.onRecordingFailed("Recorder error: " + what);
            });

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Recording STARTED: " + outputFilePath);
            return outputFilePath;

        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Recording failed: " + e.getMessage());
            releaseRecorder();
            if (listener != null) listener.onRecordingFailed(e.getMessage());
            return null;
        }
    }

    /**
     * Copies the recorded file to Music/SOS_Recordings on the phone
     * so it's visible in File Manager and playable in Music apps.
     */
    private void copyToPhoneStorage(String sourcePath, String fileName) {
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists() || sourceFile.length() == 0) {
                if (listener != null) listener.onRecordingFailed("Recording file empty");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — use MediaStore (no permission needed)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
                values.put(MediaStore.Audio.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MUSIC + "/SOS_Recordings");

                Uri uri = context.getContentResolver()
                        .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (InputStream in = new java.io.FileInputStream(sourceFile);
                         java.io.OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                        byte[] buf = new byte[4096];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    }
                    displayPath = Environment.DIRECTORY_MUSIC + "/SOS_Recordings/" + fileName;
                    Log.d(TAG, "Saved to phone Music folder: " + displayPath);
                    if (listener != null) listener.onRecordingComplete(displayPath);
                }
            } else {
                // Android 9 and below — save directly to Music folder
                File musicDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "SOS_Recordings");
                if (!musicDir.exists()) musicDir.mkdirs();

                File destFile = new File(musicDir, fileName);
                copyFile(sourceFile, destFile);
                displayPath = destFile.getAbsolutePath();
                Log.d(TAG, "Saved to: " + displayPath);
                if (listener != null) listener.onRecordingComplete(displayPath);
            }

            // Delete internal temp file
            sourceFile.delete();

        } catch (Exception e) {
            Log.e(TAG, "Copy to phone storage failed: " + e.getMessage());
            // Fallback — use internal path
            if (listener != null) listener.onRecordingComplete(sourcePath);
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (java.io.FileInputStream in = new java.io.FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Recording stopped");
            } catch (RuntimeException e) {
                Log.e(TAG, "stop() failed: " + e.getMessage());
                if (outputFilePath != null) new File(outputFilePath).delete();
                outputFilePath = null;
            } finally {
                releaseRecorder();
            }
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try { mediaRecorder.release(); } catch (Exception ignored) {}
            mediaRecorder = null;
        }
        isRecording = false;
    }

    public boolean isRecording() { return isRecording; }
    public String getOutputFilePath() { return outputFilePath; }
    public void release() { stopRecording(); }
}