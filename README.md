# MaliFlow - Personal Budget Tracker

**MaliFlow** is an Android application built in Kotlin designed to help students and young adults track their spending habits, stick to a budget, and make managing money engaging through gamification. 

This project was developed for **OPSC6311** POE (Portfolio of Evidence).

## 📱 App Demonstration Video
Watch the full walkthrough of the app running on a physical Android device below:
*   **YouTube Video:** [INSERT UNLISTED YOUTUBE LINK HERE]

## ✨ The 2 Original Features (For Lecturer Review)
As required by the brief, I have implemented two original features beyond the minimum requirements. Please look out for these when evaluating:
1.  **Daily Budget Allowance Indicator:** On the home dashboard, the app automatically calculates how much money the user can safely spend *today* by dividing the remaining monthly budget by the days left in the month.
2.  **Rollover Budget Option:** Users can toggle a "Rollover" setting per category. If enabled, any unspent budget from the previous month is automatically added to the current month's maximum limit, rewarding disciplined spending.

## 🚀 Minimum & Core Features Implemented
*   **User Authentication:** Register and login securely using local RoomDB.
*   **Category Management:** Create custom expense categories with emojis.
*   **Expense Logging:** Add expenses with amount, date, start/end time, description, and category.
*   **Receipt Photos:** Optionally attach a photo of a receipt to any expense.
*   **Budget Goals:** Set minimum and maximum monthly budget goals and per-category limits.
*   **Filterable Views:** View expense lists and category totals by week, month, or custom date range.
*   **Graphs & Visuals:** Bar charts for daily spending against goals, and pie charts for category breakdowns.
*   **Visual Dashboard:** Progress bars that turn red when a category is overspent.
*   **Gamification (Flow Points):** Earn points and unlock badges for daily streaks and meeting budget goals.

## 🛠️ Built With
*   **Language:** Kotlin
*   **Database:** RoomDB (SQLite) for fully offline local storage.
*   **Architecture:** Activity-based architecture with ViewBinding.
*   **CI/CD:** GitHub Actions.

## ⚙️ GitHub Actions & Automated Testing
This repository is integrated with **GitHub Actions** for Continuous Integration. 
*   Every time code is pushed to the `main` branch, a workflow automatically triggers.
*   The workflow runs automated Unit Tests to ensure logic functionality is not broken.
*   The workflow builds the Debug APK to verify the code compiles successfully in a clean cloud environment.

## 📲 App Distribution (Firebase)
The built APK has been distributed for testing using Firebase App Distribution.
*   **Invite Link:** https://appdistribution.firebase.dev/i/62fd7e80a03a3853
*   **Release Link:** https://appdistribution.firebase.google.com/testerapps/1:626204447933:android:b90073b0697316fad153d4/releases/07ql8h0cik368

## 💻 How to Run the Project Locally
1. Clone this repository: `git clone [INSERT YOUR GITHUB REPO LINK HERE]`
2. Open the project in **Android Studio**.
3. Allow Gradle to sync and download dependencies.
4. Click **Run** (the green play button) to launch the app on an emulator or connected physical device.

## 📚 References
*   Android Developers. (2024). *Room Persistence Library*.
*   Android Developers. (2024). *Navigation Component*.
*   GitHub Docs. (2024). *GitHub Actions Documentation*.
*   Firebase. (2024). *App Distribution Documentation*.
