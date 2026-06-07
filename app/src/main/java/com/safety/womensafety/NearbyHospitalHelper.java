package com.safety.womensafety;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * NearbyHospitalHelper finds hospitals near the SOS location.
 *
 * SETUP REQUIRED:
 * 1. Enable "Places API" in Google Cloud Console.
 * 2. Replace YOUR_GOOGLE_MAPS_API_KEY below.
 * 3. Add INTERNET permission in AndroidManifest.xml (already needed for SMS too).
 */
public class NearbyHospitalHelper {

    private static final String TAG = "NearbyHospital";
    // ⚠️ Replace with your Google Maps API key
    private static final String MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";
    private static final int SEARCH_RADIUS_METERS = 3000; // 3 km radius

    public static class Hospital {
        public String name;
        public String vicinity;
        public double latitude;
        public double longitude;

        Hospital(String name, String vicinity, double lat, double lng) {
            this.name = name;
            this.vicinity = vicinity;
            this.latitude = lat;
            this.longitude = lng;
        }

        public String getGoogleMapsLink() {
            return "https://maps.google.com/?q=" + latitude + "," + longitude;
        }

        public String getDirectionsLink(double fromLat, double fromLng) {
            return "https://www.google.com/maps/dir/" + fromLat + "," + fromLng
                    + "/" + latitude + "," + longitude;
        }
    }

    public interface OnHospitalsFoundListener {
        void onHospitalsFound(List<Hospital> hospitals);
        void onError(String error);
    }

    public static void findNearbyHospitals(double latitude, double longitude,
                                           OnHospitalsFoundListener listener) {
        new FetchHospitalsTask(latitude, longitude, listener).execute();
    }

    /**
     * Opens Google Maps directly to show nearby hospitals — no API key needed!
     * Fallback option when API key is not set up.
     */
    public static void openNearbyHospitalsInMaps(Context context, double latitude, double longitude) {
        String uri = "https://www.google.com/maps/search/hospital/@"
                + latitude + "," + longitude + ",14z";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // If Google Maps app not installed, open in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(browserIntent);
        }
    }

    /**
     * Get directions to the nearest hospital in Google Maps.
     */
    public static void getDirectionsToHospital(Context context,
                                               double fromLat, double fromLng,
                                               double toLat, double toLng) {
        String uri = "https://www.google.com/maps/dir/" + fromLat + "," + fromLng
                + "/" + toLat + "," + toLng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);
    }

    private static class FetchHospitalsTask extends AsyncTask<Void, Void, List<Hospital>> {
        private double latitude, longitude;
        private OnHospitalsFoundListener listener;
        private String errorMessage;

        FetchHospitalsTask(double lat, double lng, OnHospitalsFoundListener listener) {
            this.latitude = lat;
            this.longitude = lng;
            this.listener = listener;
        }

        @Override
        protected List<Hospital> doInBackground(Void... voids) {
            List<Hospital> hospitals = new ArrayList<>();
            try {
                String urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                        + "?location=" + latitude + "," + longitude
                        + "&radius=" + SEARCH_RADIUS_METERS
                        + "&type=hospital"
                        + "&key=" + MAPS_API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONArray("results");

                for (int i = 0; i < Math.min(results.length(), 5); i++) {
                    JSONObject place = results.getJSONObject(i);
                    String name = place.getString("name");
                    String vicinity = place.optString("vicinity", "");
                    JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                    double lat = loc.getDouble("lat");
                    double lng = loc.getDouble("lng");
                    hospitals.add(new Hospital(name, vicinity, lat, lng));
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error fetching hospitals: " + errorMessage);
            }
            return hospitals;
        }

        @Override
        protected void onPostExecute(List<Hospital> hospitals) {
            if (listener != null) {
                if (errorMessage != null && hospitals.isEmpty()) {
                    listener.onError(errorMessage);
                } else {
                    listener.onHospitalsFound(hospitals);
                }
            }
        }
    }
}