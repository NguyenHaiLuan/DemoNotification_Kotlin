package com.example.demonotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Default Title"
        val message = intent?.getStringExtra("message") ?: "Default Message"
        val requestCode = intent?.getIntExtra("requestCode", 0) ?: 0

        // Gọi NotificationHelper để hiển thị thông báo với notificationId
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message, requestCode)
    }
}