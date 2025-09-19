package com.example.demonotification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.demonotification.ui.theme.DemoNotificationTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Quyền thông báo bị từ chối, không thể hiển thị thông báo.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            DemoNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        onShowNotification = {
                            notificationHelper.showNotification(
                                title = "HEHEHE",
                                message = "Đây là thông báo từ app demo abcd djfnsdjk djfdkjf jdfsdkf oidfhasd jasdfnhs"
                            )
                        },
                        onScheduleNotification = {
                            scheduleNotification(5_000L)
                        },
                        onScheduleSpecificTime = { hour, minute ->
                            scheduleNotificationAtSpecificTime(hour, minute)
                        },
                        modifier = Modifier.padding(innerPadding),
                        context = this
                    )
                }
            }
        }
    }

    private fun scheduleNotification(delayMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                this,
                "Vui lòng cấp quyền lên lịch chính xác trong Cài đặt!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", "Scheduled Notification")
            putExtra("message", "Đây là thông báo được lên lịch sau $delayMillis ms!")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + delayMillis

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Toast.makeText(this, "Đã lên lịch thông báo sau ${delayMillis / 1000} giây", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Lỗi khi lên lịch: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleNotificationAtSpecificTime(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                this,
                "Vui lòng cấp quyền lên lịch chính xác trong Cài đặt!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", "Scheduled Notification")
            putExtra("message", "Thông báo được lên lịch vào ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Toast.makeText(this, "Đã lên lịch thông báo vào ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Lỗi khi lên lịch: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(
    onShowNotification: () -> Unit,
    onScheduleNotification: () -> Unit,
    onScheduleSpecificTime: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onShowNotification,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Show Notification")
        }
        Button(
            onClick = onScheduleNotification,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Lên Lịch Thông Báo (5 giây)")
        }
        Button(
            onClick = { showTimePicker = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Chọn Giờ Để Lên Lịch")
        }
        Button(
            onClick = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    return@Button
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Mở Cài Đặt Thông Báo")
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onScheduleSpecificTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("Xác Nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy")
                }
            },
            title = { Text("Chọn Giờ Thông Báo") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
