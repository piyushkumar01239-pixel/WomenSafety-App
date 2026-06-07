package com.safety.womensafety;

import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Sends SOS alert emails to ALL emergency contacts that have an email address.
 *
 * SETUP:
 * 1. build.gradle: implementation 'com.sun.mail:android-mail:1.6.7'
 *                  implementation 'com.sun.mail:android-activation:1.6.7'
 * 2. Use Gmail App Password:
 *    Google Account → Security → 2-Step Verification → App Passwords → Generate
 * 3. Replace SENDER_EMAIL and SENDER_APP_PASSWORD below.
 */
public class EmailNotificationHelper {

    private static final String TAG = "EmailHelper";

    // ⚠️ Replace with your Gmail + App Password
    private static final String SENDER_EMAIL = "piyushff114@gmail.com";
    private static final String SENDER_APP_PASSWORD = "byem ekjc uwpx cttt";

    public interface EmailCallback {
        void onSuccess(int sentCount);
        void onFailure(String error);
    }

    /**
     * Send SOS email to ALL contacts that have an email address.
     * Also sends to user's own registered email as backup record.
     */
    public static void sendSOSToAllContacts(List<Contact> contacts,
                                            String userEmail,
                                            String userName,
                                            double latitude,
                                            double longitude,
                                            String address,
                                            String audioPath,
                                            EmailCallback callback) {
        new SendToAllTask(contacts, userEmail, userName,
                latitude, longitude, address, audioPath, callback).execute();
    }

    private static class SendToAllTask extends AsyncTask<Void, Void, Integer> {
        private final List<Contact> contacts;
        private final String userEmail, userName, address, audioPath;
        private final double latitude, longitude;
        private final EmailCallback callback;
        private String errorMessage;

        SendToAllTask(List<Contact> contacts, String userEmail, String userName,
                      double lat, double lng, String address,
                      String audioPath, EmailCallback callback) {
            this.contacts = contacts;
            this.userEmail = userEmail;
            this.userName = userName;
            this.latitude = lat;
            this.longitude = lng;
            this.address = address;
            this.audioPath = audioPath;
            this.callback = callback;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int sentCount = 0;
            try {
                Session session = createSession();
                String mapsLink = "https://maps.google.com/?q=" + latitude + "," + longitude;
                String body = buildEmailBody(userName, latitude, longitude, address, mapsLink, audioPath);
                String subject = "🚨 SOS ALERT - " + userName + " Needs Help!";

                // 1. Send to all personal contacts that have email
                for (Contact contact : contacts) {
                    if (contact.hasEmail()) {
                        try {
                            sendEmail(session, contact.getEmail(), subject, body);
                            sentCount++;
                            Log.d(TAG, "Email sent to contact: " + contact.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to send to " + contact.getName() + ": " + e.getMessage());
                        }
                    }
                }

                // 2. Always send to user's own email as backup record
                if (userEmail != null && !userEmail.isEmpty()) {
                    try {
                        String backupSubject = "🚨 [YOUR SOS RECORD] - " + subject;
                        String backupBody = "<p style='background:#fff3cd;padding:10px;'>"
                                + "⚠️ This is a copy of the SOS alert sent from your account.</p>" + body;
                        sendEmail(session, userEmail, backupSubject, backupBody);
                        sentCount++;
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to send backup to user: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Email session failed: " + errorMessage);
            }
            return sentCount;
        }

        @Override
        protected void onPostExecute(Integer sentCount) {
            if (callback != null) {
                if (sentCount > 0) callback.onSuccess(sentCount);
                else callback.onFailure(errorMessage != null ? errorMessage : "No emails sent");
            }
        }
    }

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        return Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });
    }

    private static void sendEmail(Session session, String toEmail,
                                  String subject, String body) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");
        Transport.send(message);
    }

    private static String buildEmailBody(String userName, double lat, double lng,
                                         String address, String mapsLink, String audioPath) {
        // Audio section
        String audioSection = "";
        if (audioPath != null && !audioPath.isEmpty()) {
            audioSection = "<div style='background:#fff3cd;border-left:4px solid #ffc107;"
                    + "padding:12px;margin:16px 0;border-radius:4px;'>"
                    + "<p style='margin:0;'><strong>🎙️ Audio Recording Saved</strong></p>"
                    + "<p style='margin:6px 0 0;font-family:monospace;font-size:12px;color:#555;'>"
                    + audioPath + "</p>"
                    + "<p style='margin:4px 0 0;font-size:12px;color:#888;'>"
                    + "Audio is saved on the device. Retrieve device to access the recording.</p>"
                    + "</div>";
        } else {
            audioSection = "<div style='background:#f8f9fa;border-left:4px solid #ccc;"
                    + "padding:10px;margin:16px 0;'>"
                    + "<p style='margin:0;font-size:12px;color:#888;'>"
                    + "🎙️ Audio recording not available (microphone permission may be off)</p>"
                    + "</div>";
        }

        // Emergency numbers section
        String emergencySection = "<div style='background:#fde8e8;border-left:4px solid #e74c3c;"
                + "padding:12px;margin:16px 0;border-radius:4px;'>"
                + "<p style='margin:0;color:#c0392b;'><strong>📞 Emergency Services Also Alerted:</strong></p>"
                + "<p style='margin:8px 0 0;'>"
                + "🚓 <strong>100</strong> Police &nbsp;|&nbsp; "
                + "🚑 <strong>108</strong> Ambulance &nbsp;|&nbsp; "
                + "🆘 <strong>112</strong> National Emergency"
                + "</p></div>";

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                + "<div style='background:#e74c3c;padding:24px;text-align:center;'>"
                + "<h1 style='color:white;margin:0;'>🚨 EMERGENCY SOS ALERT</h1>"
                + "</div>"
                + "<div style='padding:24px;background:#f8f9fa;'>"
                + "<h2 style='color:#e74c3c;margin-top:0;'>" + userName + " needs immediate help!</h2>"
                + "<p style='font-size:16px;'><strong>📍 Location:</strong> " + address + "</p>"
                + "<p style='font-size:16px;'><strong>🗺️ Coordinates:</strong> " + lat + ", " + lng + "</p>"
                + "<p><a href='" + mapsLink + "' style='background:#e74c3c;color:white;padding:14px 28px;"
                + "text-decoration:none;border-radius:6px;font-size:16px;font-weight:bold;display:inline-block;'>"
                + "📍 Open in Google Maps</a></p>"
                + audioSection
                + emergencySection
                + "<hr style='border:none;border-top:1px solid #ddd;'/>"
                + "<p style='color:#888;font-size:12px;'>Automated alert from Women Safety SOS App. "
                + "Please contact " + userName + " or call 112 immediately.</p>"
                + "</div></body></html>";
    }
}