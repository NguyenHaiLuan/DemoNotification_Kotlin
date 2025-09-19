package com.example.demonotification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "local_channel_01"

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Local Notifications"
            val descriptionText = "Nhớ đứng dậy đi tới đi lui"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                val customSoundUri: Uri =
                    "android.resource://${context.packageName}/${R.raw.alert}".toUri()
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(customSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("NotificationHelper", "Không có quyền POST_NOTIFICATIONS, không thể hiển thị thông báo")
                return
            }
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w("NotificationHelper", "Thông báo bị vô hiệu hóa bởi hệ thống hoặc người dùng")
            return
        }

        // PendingIntent để mở MainActivity khi nhấn thông báo chính
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent để mở Google khi nhấn action button
        val googleIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val googlePendingIntent = PendingIntent.getActivity(
            context,
            1,
            googleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.twotone_4g_plus_mobiledata_24, "Mở Google", googlePendingIntent)

        if (notificationId == 100) {
            // Dùng BigPictureStyle cho thông báo ngay lập tức
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.broad_image)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(message)
            )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }


        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            Log.d("NotificationHelper", "Thông báo hiển thị với title: $title, ID: $notificationId")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Lỗi khi hiển thị thông báo: ${e.message}")
        }
    }
}