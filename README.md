# 🛡️ Women Safety SOS Alert System

## Description
A native Android emergency safety application designed to protect women in critical situations. With a single tap or by shaking the phone 3 times, the app instantly captures GPS location, records 30-second audio, and sends simultaneous SMS + Email alerts to all emergency contacts. Features one-tap calling to national emergency services (112, 100, 108) and nearby hospital detection.

> **Status:** ✅ 100% Complete | Tested on Real Device (Android 15)

## Features
- 🆘 **One-Touch SOS Button** — Large SOS button with 5-second countdown to prevent false alarms
- 📳 **Shake Detection** — Shake phone 3 times to trigger SOS automatically (works with screen off)
- 📍 **GPS Location Tracking** — Real-time coordinates sent as a clickable Google Maps link
- 📱 **SMS Alerts** — Instant SMS to all saved emergency contacts with full address
- 📧 **Email Notifications** — HTML-formatted email alerts to all contacts with email addresses
- 🎙️ **Audio Recording** — 30-second auto-recording saved to `Music/SOS_Recordings/` on phone
- ▶️ **Audio Playback** — Play/Stop recorded audio directly in Alert History screen
- 📞 **Emergency Call Dialog** — One-tap calling to 112 (Emergency), 100 (Police), 108 (Ambulance)
- 🏥 **Nearby Hospitals** — Find and navigate to nearest hospitals via Google Maps
- ✅ **I'm Safe Button** — Cancel SOS, stops recording, notifies all contacts with safety confirmation
- 👤 **User Profile** — Store blood group, medical info, and home address for first responders
- 🕐 **Alert History** — Full log of SOS events with location tap-to-map and audio playback
- 💾 **SQLite Database** — All data stored locally — works fully offline

## Technologies Used
- **Language:** Java
- **Platform:** Android (API 23–36 / Android 6.0–14)
- **Database:** SQLite (WomenSafetyDB v4)
- **IDE:** Android Studio
- **Email:** JavaMail API (android-mail 1.6.7) via Gmail SMTP
- **Location:** Android LocationManager + Geocoder
- **SMS:** Android SmsManager
- **Audio:** Android MediaRecorder + MediaPlayer
- **Sensors:** SensorManager (Accelerometer for shake detection)
- **Maps:** Google Maps Intent + Google Places API (optional)
- **Build:** Gradle

## Project Structure
com.safety.womensafety/
├── MainActivity.java              # Home screen, SOS logic, shake & GPS
├── LoginActivity.java             # User authentication
├── RegisterActivity.java          # New user registration
├── SplashActivity.java            # Launch screen
├── EmergencyContactsActivity.java # Manage emergency contacts
├── UserProfileActivity.java       # Edit profile & medical info
├── AlertHistoryActivity.java      # View past SOS alerts with audio playback
├── DatabaseHelper.java            # SQLite CRUD operations
├── ShakeDetector.java             # Accelerometer shake detection
├── AudioRecorderHelper.java       # 30-second audio recording
├── EmailNotificationHelper.java   # Gmail SMTP email alerts
├── NearbyHospitalHelper.java      # Google Places hospital finder
├── ContactAdapter.java            # Emergency contacts list adapter
└── AlertHistoryAdapter.java       # Alert history list with audio player

## Installation
1. Clone the repository
```bash
   git clone https://github.com/YOUR_USERNAME/WomenSafetyApp.git
```
2. Open in **Android Studio**
3. Go to **File → Sync Project with Gradle Files**
4. Add JavaMail dependency in `build.gradle (Module: app)`:
```gradle
   implementation 'com.sun.mail:android-mail:1.6.7'
   implementation 'com.sun.mail:android-activation:1.6.7'
```
5. *(Optional)* Set up Gmail App Password in `EmailNotificationHelper.java`:
```java
   private static final String SENDER_EMAIL = "your@gmail.com";
   private static final String SENDER_APP_PASSWORD = "your_app_password";
```
6. *(Optional)* Add Google Maps API Key in `NearbyHospitalHelper.java` for hospital finder
7. Connect Android device or start emulator
8. Click **Run ▶️**

## Permissions Required
| Permission | Purpose |
|-----------|---------|
| `SEND_SMS` | Send emergency SMS to contacts |
| `ACCESS_FINE_LOCATION` | GPS location during SOS |
| `RECORD_AUDIO` | 30-second audio recording |
| `CALL_PHONE` | One-tap call to 112/100/108 |
| `INTERNET` | Email alerts & hospital finder |
| `VIBRATE` | Vibration feedback on SOS |

## Database Schema
**3 Tables:** `users` · `emergency_contacts` · `alert_history`
- Supports DB version auto-upgrade without data loss
- All data stored locally — no internet required for core features

## How SOS Works
Tap SOS / Shake 3x
↓
5s Countdown (cancellable)
↓
Audio Recording Starts 🎙️
↓
GPS Location Captured 📍
↓
SMS → All Contacts 📱
↓
Email → Contacts with Email 📧
↓
Dialog: Call 112 / 100 / 108 📞
↓
Alert Saved to History 🕐

## Screenshots
> *(Add screenshots here)*

## Team
| Name | Roll Number |
|------|------------|
| Piyush Kumar | 24BCS11092 |
| Tokid Ahmed | 24BCS10063 |
| Rituraj | 24BCS11049 |
| Morvika Sharma | 24BCS10318 |
| Sneh Sharma | 24BCS11098 |

**Submitted to:** Dr. Damandeep Kaur | April 2026

## Project Report & Source Code
📁 [Google Drive — Full Project Files](https://drive.google.com/drive/folders/12b29JR4w3ClgwYqTRdY_pJOUll9ryVpa?usp=sharing)

## License
MIT License — feel free to use and modify with attribution.
