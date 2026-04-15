Small Things Matter 👇

---

# 📈 BeiTracker — Smart Price Intelligence for Tanzania

<p align="center">
  <img src="assets/images/hero.png" width="800"/>
</p>

<p align="center">
  <strong>Track prices. Understand trends. Buy smarter.</strong>
</p>

---

## ✨ Features

### 🔍 Smart Search

<p align="center">
  <img src="assets/images/search.png" width="260"/>
  <img src="assets/images/search_results.png" width="260"/>
</p>

* Search products across multiple marketplaces (e.g., Jiji)
* Intelligent query handling (e.g., *“cheap iPhone under 500k”*)
* Real-time suggestions & history

---

### 📊 Price Intelligence

<p align="center">
  <img src="assets/images/chart.png" width="260"/>
  <img src="assets/images/price_stats.png" width="260"/>
</p>

* Time-series price charts
* Track price changes over time
* View min, max, and average prices
* Detect price drops with percentage insights

---

### 🔔 Price Alerts

<p align="center">
  <img src="assets/images/alerts.png" width="260"/>
</p>

* Get notified when prices drop
* Personalized tracking per user
* Configurable notification intervals

---

### 🧠 Market Insights

<p align="center">
  <img src="assets/images/trending.png" width="260"/>
</p>

* Trending products
* Most tracked items
* Top price drops
* Community-driven signals

---

### ❤️ Personal Tracking

<p align="center">
  <img src="assets/images/dashboard.png" width="260"/>
</p>

* Save and monitor products
* Recently viewed items
* Clean and intuitive dashboard

---

### 💬 Chat System (Future-ready)

<p align="center">
  <img src="assets/images/chat.png" width="260"/>
</p>

* Real-time messaging
* Product discussions (group or 1:1)

---

## 🏗️ Architecture

BeiTracker is designed as a **data-driven, scalable platform**:

* **Frontend:** Kotlin + Jetpack Compose
* **Backend:** Firebase (Firestore, Cloud Functions, FCM)
* **Architecture Pattern:** MVVM
* **Dependency Injection:** Hilt

---

## 🗄️ Firestore Structure (Simplified)

```
products/{productId}
    ├── name, model, category
    ├── currentPrice, minPrice, maxPrice, avgPrice
    └── lastUpdated

products/{productId}/priceHistory/{entryId}
    └── price, source, url, timestamp

products/{productId}/listings/{listingId}
    └── price, source, url, lastSeen

users/{userId}/trackedItems/{productId}
    └── productId, addedAt, lastNotifiedPrice

product_watchers/{productId}
    └── userIds[]
```

---

## ⚡ Performance & Scalability

* Optimized Firestore reads/writes
* Time-series data stored in subcollections
* Aggregated metrics precomputed
* Designed for **10K–100K+ users**

---

## 🔐 Security

* Firebase Authentication for user management
* Firestore Security Rules (planned / configurable)
* Scoped access per user

---

## 🚀 Getting Started

### Prerequisites

* Android Studio (latest)
* Firebase project
* Android device or emulator

---

### 🔧 Setup

1. Clone the repository:

```bash
git clone https://github.com/your-username/beitracker.git
```

2. Open in Android Studio

3. Add your Firebase config:

   * Download `google-services.json`
   * Place it in `/app` directory

4. Enable Firebase services:

   * Firestore
   * Authentication
   * Cloud Messaging

5. Run the app 🚀

---

## 📦 Tech Stack

* **Kotlin**
* **Jetpack Compose**
* **Firebase Firestore**
* **Firebase Cloud Messaging (FCM)**
* **Hilt (DI)**
* **Coroutines & Flow**

---

## 🧠 Vision

> 📊 **The intelligence layer for the African market**

Helping users:

* avoid overpaying
* understand market trends
* make data-driven decisions

---

## 🔮 Roadmap

* [x] AI-powered recommendations
* [x] Advanced price predictions
* [x] Seller integrations
* [x] Web platform (SEO traffic)
* [ ] WhatsApp tracking bot

---

## 👤 Author

**Devi Doa**
Android Developer | Building data-driven products

---

## ⭐ Support

If you like this project:

👉 Star the repo
👉 Share it
👉 Contribute

---

<p align="center">
  Built with ❤️ for smarter shopping in Tanzania 🇹🇿
</p>

---
