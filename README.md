# 🐛 Reshme Namma Pride
### 🌱 AI-Powered Silkworm Farming Assistant

> Smart sericulture management using Android, AI, and climate analytics.

---

## 📌 Overview

**Reshme Namma Pride** is an AI-powered Android application designed to help silkworm farmers monitor climate conditions, manage silkworm batches, and improve cocoon yield using intelligent farming recommendations.

The application combines:

- 📱 Modern Android development
- 🤖 Google Gemini AI
- 🌡️ Climate trend analysis
- 🗄️ Local database management
- 📊 Smart farming insights

to provide a complete digital assistant for sericulture farming.

---

# ✨ Features

## 🐛 Batch Management
- Create and manage silkworm batches
- Store breed details and batch history
- Track batch growth lifecycle

## 🌡️ Climate Monitoring
- Record temperature and humidity
- Maintain climate history logs
- Detect unstable climate conditions

## 🤖 AI Farming Advisor
- AI-powered climate analysis
- Growth optimization suggestions
- Humidity and temperature recommendations
- Smart farming insights using Google Gemini AI

## 📊 Climate Data Processing
- Average climate analysis
- Trend detection
- Stability analysis
- Risk condition detection

## 🎨 Modern Android UI
- Built fully with Jetpack Compose
- Material 3 design
- Smooth navigation flow
- Animated welcome screen
- Clean farming-themed interface

---

# 🏗️ System Architecture

![Architecture](assets/architecture.png)

---

# ⚙️ Tech Stack

## 📱 Android Development
- Kotlin
- Jetpack Compose
- Navigation Compose
- Material 3
- Coroutines

## 🧠 Architecture
- MVVM Architecture
- Repository Pattern
- StateFlow
- ViewModel

## 🗄️ Database
- Room Database
- DAO Pattern
- SQLite

## 🌐 Networking
- Retrofit
- OkHttp
- Kotlinx Serialization

## 🤖 AI Integration
- Google Gemini API
- Gemini 2.0 Flash
- Prompt Engineering
- Climate Data Preprocessing

## 🛠️ Tools
- Android Studio
- Git
- GitHub
- Gradle

---

# 📂 Project Structure

```text
app/
 ├── ai/
 │    ├── dto/
 │    ├── preprocessing/
 │    └── service/
 │
 ├── database/
 │    ├── dao/
 │    └── entity/
 │
 ├── model/
 ├── navigation/
 ├── repository/
 ├── ui/
 │    ├── screens/
 │    └── theme/
 │
 ├── viewmodel/
 └── MainActivity.kt
```

---

# 🔄 AI Workflow

```text
Farmer Climate Entries
          ↓
Climate Preprocessing
          ↓
Average + Trend Analysis
          ↓
Prompt Builder
          ↓
Google Gemini AI
          ↓
Smart Farming Recommendations
```

---

# 📸 Screenshots

## 🌿 Welcome Screen
![Welcome Screen](screenshots/welcome_screen.png)

---

## 🏠 Home Screen
![Home Screen](screenshots/home_screen.png)

---

## 📊 Dashboard
![Dashboard](screenshots/dashboard_screen.png)

---

## 🤖 AI Analysis
![AI Analysis](screenshots/ai_analysis.png)

---

# 🚀 Setup Instructions

## 1️⃣ Clone Repository

```bash
git clone https://github.com/ashxk024/ReshmeNammaPride.git
```

---

## 2️⃣ Open in Android Studio

Open the project folder in Android Studio.

---

## 3️⃣ Add Gemini API Key

Create/Edit:

```text
local.properties
```

Add:

```properties
GEMINI_API_KEY=YOUR_API_KEY_HERE
```

---

## 4️⃣ Sync Gradle

```text
File → Sync Project with Gradle Files
```

---

## 5️⃣ Run Application

Connect an Android device/emulator and run the app.

---

# 🎯 Future Improvements

- ☁️ Firebase cloud synchronization
- 📡 IoT sensor integration
- 🌍 Multi-language farmer support
- 📈 Advanced analytics dashboard
- 📄 PDF report generation
- 🔔 Smart climate alerts
- 🛰️ Real-time weather integration

---

# 💡 Problem Statement

Traditional silkworm farming often depends on manual climate monitoring and farmer experience, which can lead to inconsistent cocoon quality and reduced silk production.

This project aims to digitize and modernize sericulture farming using AI-driven climate analysis and smart recommendations.

---

# 👨‍💻 Author

### Ashok

Android Developer | AI Enthusiast | Smart Agriculture Explorer

---

# ⭐ If you like this project

Give this repository a ⭐ on GitHub!

---

# 📜 License

This project is developed for educational and research purposes.