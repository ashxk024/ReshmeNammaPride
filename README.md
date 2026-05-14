\# Reshme Namma Pride



AI-powered silkworm farming assistant built using Android, Jetpack Compose, Room Database, and Google Gemini AI.



\## Overview



Reshme Namma Pride helps silkworm farmers monitor climate conditions, manage silkworm batches, and receive AI-generated farming recommendations to improve cocoon quality and silk yield.



The application stores temperature and humidity records locally, analyzes trends, and uses Google Gemini AI to generate practical farming advice.



\---



\## Features



\### Batch Management

\- Create and manage multiple silkworm batches

\- Persistent local storage using Room Database

\- Batch dashboard with climate history



\### Climate Monitoring

\- Record:

&#x20; - Temperature

&#x20; - Humidity

\- View climate history for each batch

\- Climate stability analysis



\### AI Farming Advisor

\- Climate trend preprocessing

\- Average/min/max analysis

\- Instability detection

\- Google Gemini AI integration

\- AI-generated recommendations for:

&#x20; - Growth improvement

&#x20; - Humidity management

&#x20; - Temperature correction

&#x20; - Silk quality improvement



\### Modern Android UI

\- Built fully with Jetpack Compose

\- Custom splash screen

\- Welcome screen

\- Material 3 design

\- Responsive UI



\---



\## Tech Stack



\### Android

\- Kotlin

\- Jetpack Compose

\- Navigation Compose

\- ViewModel

\- Coroutines



\### Database

\- Room Database

\- DAO Architecture

\- StateFlow



\### Networking

\- Retrofit

\- OkHttp

\- Kotlinx Serialization



\### AI

\- Google Gemini API

\- Gemini 2.0 Flash



\---



\## Project Structure



```text

app/

&#x20;├── ai/

&#x20;├── database/

&#x20;├── navigation/

&#x20;├── repository/

&#x20;├── ui/

&#x20;├── viewmodel/

&#x20;└── model/



\##Screens

Welcome Screen

Home Screen

New Batch Screen

Dashboard Screen

AI Analysis Panel





\##Setup

1\. Clone Repository

git clone https://github.com/ashxk024/ReshmeNammaPride.git



2\. Open in Android Studio

Open the project folder in Android Studio.



3\. Add Gemini API Key



Create or edit:



local.properties



Add:

GEMINI\_API\_KEY=YOUR\_API\_KEY\_HERE



4\. Sync Gradle

File → Sync Project with Gradle Files



5\. Run App

Connect device/emulator and run the project.



\##Future Improvements

Firebase cloud sync

IoT sensor integration

Real-time climate monitoring

Multi-language support

PDF report export

Farmer analytics dashboard

Offline AI caching





\##License



This project is for educational and research purposes.









