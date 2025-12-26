# 🌸 CircleBloom

**Smart Study Partner & Skill Exchange Platform**

> *Learn Together, Grow Together*

CircleBloom is an AI-powered mobile application that connects university students to study together and exchange skills. The concept is similar to a matching app, but focused on **academic collaboration and skill development**.

---

## Setup & Installation

### Prerequisites

Make sure the following are installed:

* **Android Studio (latest stable)**
* **JDK 8+**
* **Android SDK**
* **Firebase account**
* Android emulator or physical device

---

### 1. Clone Repository

```bash
git clone https://github.com/keira1934/CircleBloom.git
cd CircleBloom
```

---

### 2. Open Project in Android Studio

1. Open **Android Studio**
2. Select **Open an Existing Project**
3. Choose the `CircleBloom` folder
4. Wait for **Gradle Sync** to complete

> ⚠️ If Gradle errors occur, ensure your Gradle and JDK versions match the project configuration.

---

### 3. Firebase Configuration

CircleBloom uses **Firebase** as its backend.

1. Create a new project in **Firebase Console**
2. Add an Android app

   * Package name: match the `applicationId` in `build.gradle`
3. Download `google-services.json`
4. Place the file in:

```text
app/google-services.json
```

5. Enable the following Firebase services:

   * Authentication (Email & Google Sign-In)
   * Real-time Database

---

### 4. Build & Run the Application

1. Select a device (emulator or physical device)
2. Click **Run ▶** in Android Studio
3. Wait for the build process to finish
4. CircleBloom will launch on your device

---
## Troubleshooting

**Gradle Build Failed**

* Ensure a stable internet connection
* Verify JDK and Gradle versions
* Use *Invalidate Caches & Restart*

**Firebase Errors**

* Ensure `google-services.json` is correctly placed
* Confirm Authentication and Firestore are enabled

---

## Contributing

1. Fork the repository
2. Create a new branch

```bash
git checkout -b feature/your-feature-name
```

3. Commit your changes
4. Push to your branch
5. Open a Pull Request

---
## Core Concept

CircleBloom connects students based on shared academic and learning preferences, including:

* Shared courses (Study Buddy)
* Complementary skills (Skill Exchange)
* Compatible schedules
* Learning styles
* Aligned academic goals

The goal is to transform a typically solitary learning experience into one that is **collaborative, effective, and enjoyable**.

---

## Key Features

### 1. Smart Matching System

**3 Match Types:**

* **Study Match** – Study partners for the same course  
* **Skill Exchange** – Skill swapping (e.g., coding ↔ design)  
* **Hybrid Match** – Combination of studying and skill exchange  

**Compatibility Score (0–100%)**

* Course overlap  
* Schedule fit  
* Learning style  
* Skill synergy
  
---

### 2. Smart Onboarding (6 Steps)

New users complete a short onboarding process (±5–7 minutes):

1. **Academic Profile** – University, major, semester, GPA  
2. **Courses** – Enrolled courses and difficulty level  
3. **Skills Inventory** – Skills you have & skills you want to learn  
4. **Learning Preferences** – Learning style & ideal session duration  
5. **Availability Schedule** – Available study hours
6. **Goals & Motivation** – Academic goals & weekly commitment  

---

### 3. Session Management

* Create study sessions (time, topic, place)  
* Join sessions
* Track sessions (upcoming, completed, cancelled)   

---

### 4. Analytics Dashboard

**Personal Insights:**

* Study hours tracking (charts)  
* Course performance prediction  
* Skill progress tracking  
* Study streak counter  

**Insights & Recommendations:**

* Match and session recommendations based on user activity

---

### 5. In-App Messaging & Notifications

* Real-time chat with matches  
* Notifications for:
  * Session reminders  
  * New compatible matches  

---

##  How to Use the App

### First-Time User

1. **Register / Login**

   * Use any valid email address or sign in with Google
2. **Email Verification**

   * Email verification is required for all registered email addresses
3. **Onboarding Wizard**

   * Complete academic profile, courses, skills, schedule, and goals
4. **Explore Matches**

   * View recommended study buddies and skill exchange partners
5. **Send Match Requests**
6. **Chat & Schedule Sessions**
7. **Attend Sessions & Rate Partners**

---

### Daily Usage Flow

* **Home** → Quick stats, upcoming sessions, and daily study timer
* **Match** → Discover and match with people who share similar goals and interests
* **Chat** → Chat with your matches and plan study sessions
* **Sessions** → Create, manage, and join study sessions
* **Analytics** → View study progress, duration, and insights of your study performance
* **Profile** → Update profile, badges, and settings

---

### Tools & Platforms
The following tools and platforms were used in the development of CircleBloom:
* **Android Studio**
Used as the primary IDE for developing, building, running, and debugging the Android application.
* **GitHub**
Used for version control, source code management, and team collaboration.
* **Jira**
Used for project management, task tracking, sprint planning, and monitoring team progress.


---

### Demo Application
You can view the CircleBloom demo application, including an app walkthrough and feature showcase, via the following Google Drive link:
* 🔗 Demo App (Google Drive):
  https://drive.google.com/drive/folders/1089zuXLPjGFvedw-OI1ssCCaD8q38rOR

---

## License

This project is developed for **academic and learning purposes**.

---

## Authors
### Created by Group 3 – Data Science Class 1
* Azzahra Puteri Kamilah (012202400070)
* Fasya Nabila Salim (012202400012)
* Marchella Keira Sambuaga (012202400010)
* Ryantinisa Guzelazkia (012202400006)

---

**CircleBloom — Learn Together, Grow Together 🌸**
