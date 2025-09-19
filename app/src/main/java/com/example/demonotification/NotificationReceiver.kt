package com.example.demonotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Default Title"
        val message = intent?.getStringExtra("message") ?: "Default Message"

        // Gọi NotificationHelper để hiển thị thông báo
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)
    }
}