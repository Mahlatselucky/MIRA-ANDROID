package com.mira.app.data

/**
 * Receives push notifications from Firebase Cloud Messaging and shows them
 * on the phone. When the app is open the notification is built here; when it
 * is in the background Firebase shows it through the "mira_updates" channel.
 * onNewToken fires when Firebase gives this phone its address for pushes.
 * See: https://firebase.google.com/docs/cloud-messaging/android/client
 */

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mira.app.MainActivity
import com.mira.app.R

class MiraMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        android.util.Log.d("MIRA", "New FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: ""
        android.util.Log.d("MIRA", "Push received: $title / $body")
        showNotification(this, title, body)
    }

    companion object {
        const val CHANNEL_ID = "mira_updates"

        /** Creates the notification channel. Safe to call more than once. */
        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MIRA updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        /** Builds and shows one notification. Tapping it opens the app. */
        fun showNotification(context: Context, title: String, body: String) {
            createChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
            context.getSystemService(NotificationManager::class.java)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
