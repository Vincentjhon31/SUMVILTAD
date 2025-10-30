package com.zynt.sumviltadconnect.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zynt.sumviltadconnect.MainActivity
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new FCM registration token is generated.
     * This is where you should send the token to your app server.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "========================================")
        Log.d(TAG, "📩 FCM MESSAGE RECEIVED!")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "========================================")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📦 Message data payload: ${remoteMessage.data}")

            // Handle data payload
            handleDataPayload(remoteMessage.data)
        } else {
            Log.d(TAG, "⚠️ No data payload in message")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "🔔 Message Notification Title: ${it.title}")
            Log.d(TAG, "🔔 Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        } ?: run {
            Log.d(TAG, "⚠️ No notification payload in message")
        }

        Log.d(TAG, "========================================")
    }

    /**
     * Handle data payload from FCM message
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"]
        val message = data["message"] ?: data["body"]
        val type = data["type"]

        Log.d(TAG, "Data - Title: $title, Message: $message, Type: $type")

        // Send notification for data messages
        sendNotification(title, message)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageTitle FCM message title received.
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageTitle: String?, messageBody: String?) {
        Log.d(TAG, "🔔 sendNotification called")
        Log.d(TAG, "Title: $messageTitle")
        Log.d(TAG, "Body: $messageBody")

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        Log.d(TAG, "Building notification with channel: $channelId")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(messageTitle ?: getString(R.string.app_name))
            .setContentText(messageBody ?: "")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Got notification manager")

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FCM Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Firebase Cloud Messaging notifications"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created/updated")
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed! (ID: $NOTIFICATION_ID)")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")

        if (token != null) {
            // Save token locally
            val sharedPreferences = getSharedPreferences("sumviltad_prefs", MODE_PRIVATE)
            sharedPreferences.edit {
                putString("fcm_token", token)
            }

            // Send token to backend server
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val authToken = sharedPreferences.getString("auth_token", null)
                    if (authToken != null) {
                        val response = ApiClient.apiService.storeFcmToken(
                            "Bearer $authToken",
                            mapOf("token" to token)
                        )
                        if (response.isSuccessful) {
                            Log.d(TAG, "FCM token sent to server successfully")
                        } else {
                            Log.e(TAG, "Failed to send FCM token: ${response.code()}")
                        }
                    } else {
                        Log.w(TAG, "No auth token found, token will be sent after login")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending FCM token to server", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val NOTIFICATION_ID = 0
    }
}

