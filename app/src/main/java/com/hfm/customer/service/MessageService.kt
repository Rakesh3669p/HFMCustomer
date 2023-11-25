package com.hfm.customer.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hfm.customer.R
import com.hfm.customer.ui.dashBoard.DashBoardActivity
import com.hfm.customer.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class MessageService : FirebaseMessagingService() {
    private val channelId = "notification_channel"
    private var audioStoryId = ""

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("Push-notification object" + remoteMessage.data)
    }


    @SuppressLint("UnspecifiedImmutableFlag", "SuspiciousIndentation")
    private fun showNotification(title: String, message: String) {
        val randoms = Random(System.currentTimeMillis()).nextInt(1000)
        val smallIcon = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.app_icon),
            512,
            512,
            false
        )


        var intent: Intent? = null

        if (intent == null) {
            intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(this, DashBoardActivity::class.java)
            } else {
                Intent(this, DashBoardActivity::class.java)
            }
        }


        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, randoms, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, randoms, intent, PendingIntent.FLAG_ONE_SHOT)
        }


        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()


        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setOngoing(false)
                .setLargeIcon(smallIcon)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "web_app", NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.apply {
                vibrationPattern = longArrayOf(0.toLong())
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    audioAttributes
                )
                importance = NotificationManager.IMPORTANCE_HIGH
                lockscreenVisibility = NotificationCompat.PRIORITY_MAX
            }
            manager.createNotificationChannel(notificationChannel)
        }

        manager.notify(randoms, builder.build())
    }
}

