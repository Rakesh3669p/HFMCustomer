package com.hfm.customer.service

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("push notification Data: ${remoteMessage.data}")
        val appTargetPage = remoteMessage.data["app_target_page"]
        val notify_type = remoteMessage.data["notify_type"]
        val ref_id = remoteMessage.data["ref_id"]
        val bulk_order_sale_id = remoteMessage.data["bulk_order_sale_id"]
        val orderId = remoteMessage.data["orderId"]
        val id = remoteMessage.data["id"]
        val title = remoteMessage.data["title"]
        val desc = remoteMessage.data["description"]
        if(title!=null) {
            showNotification(appTargetPage,
                    notify_type,
                    ref_id,
                    bulk_order_sale_id,
                    orderId,
                    id,title,desc

            )
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag", "SuspiciousIndentation")
    private fun showNotification(
        appTargetPage: String?,
        notify_type: String?,
        ref_id: String?,
        bulk_order_sale_id: String?,
        orderId: String?,
        id: String?,
        title: String?,
        desc: String?
    ) {
        val randoms = Random(System.currentTimeMillis()).nextInt(1000)
        val smallIcon = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.app_icon),
            256,
            256,
            false
        )

        val appIcon: Drawable = BitmapDrawable(resources, smallIcon)



        var intent: Intent? = null

        if (intent == null) {
            intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(this, DashBoardActivity::class.java).also {
                    it.putExtra("appTargetPage",appTargetPage)
                    it.putExtra("notify_type",notify_type)
                    it.putExtra("ref_id",ref_id)
                    it.putExtra("bulk_order_sale_id",bulk_order_sale_id)
                    it.putExtra("orderId",orderId)
                    it.putExtra("id",id)
                    it.putExtra("title",title)
                    it.putExtra("description",desc)
                }
            } else {
                Intent(this, DashBoardActivity::class.java).apply {
                    putExtra("appTargetPage",appTargetPage)
                    putExtra("notify_type",notify_type)
                    putExtra("ref_id",ref_id)
                    putExtra("bulk_order_sale_id",bulk_order_sale_id)
                    putExtra("orderId",orderId)
                    putExtra("id",id)
                    putExtra("title",title)
                    putExtra("description",desc)

                }
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
                .setSmallIcon(R.drawable.ic_notification_badge)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(desc)
                .setOngoing(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "app", NotificationManager.IMPORTANCE_HIGH)

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

