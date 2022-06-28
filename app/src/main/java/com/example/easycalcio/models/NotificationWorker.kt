package com.example.easycalcio.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.easycalcio.R
import com.example.easycalcio.activities.SplashActivity
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val notificationSharedPreferences: String = "NotificationSharedPrefs"

fun setNotificationWorkerRunning(context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        notificationSharedPreferences, MODE_PRIVATE
    )

    val myEdit = sharedPreferences.edit()

    myEdit.putBoolean("isStarted", true)
    myEdit.apply()
}

fun isNotificationWorkerRunning(context: Context): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        notificationSharedPreferences, MODE_PRIVATE
    )
    return sharedPreferences.getBoolean("isStarted", false)
}

fun runInstantWorker(context: Context) {
    val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(notificationWorker)
}

fun startPeriodicWorker(context: Context) {
    val notificationWorker = PeriodicWorkRequestBuilder<NotificationWorker>(
        15,
        TimeUnit.MINUTES,
        5,
        TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "myPeriodicWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        notificationWorker
    )

    setNotificationWorkerRunning(context)
}

class NotificationWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {

        //TODO: fix notifications

        val chats = getChats(context)
        val user = getUser(context)
        val intent = Intent(context, SplashActivity::class.java)

        if (chats != null) {
            for (chat in chats) {
                val messages = chat.messages
                if (messages != null) {
                    for (message in messages) {
                        if (message.sender != user.username && !message.read) {
                            val notificationText = message.text
                            val builder = NotificationCompat.Builder(context, "MESSAGE")
                                .setSmallIcon(R.drawable.ic_message).setContentTitle("New message")
                                .setContentText(notificationText).setStyle(
                                    NotificationCompat.BigTextStyle().bigText(notificationText)
                                ).setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setAutoCancel(true)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val name = "chat"
                                val descriptionText = "chat"
                                val importance = NotificationManager.IMPORTANCE_DEFAULT
                                val channel =
                                    NotificationChannel("MESSAGE", name, importance).apply {
                                        description = descriptionText
                                    }

                                val notificationManager: NotificationManager =
                                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.createNotificationChannel(channel)
                            }

                            with(NotificationManagerCompat.from(context)) {
                                notify(chat.id.toInt(), builder.build())
                            }
                        }
                    }
                }
            }
        }

        val friendRequests = getReceivedRequests(context)

        if (friendRequests != null) {
            for (request in friendRequests) {
                val notificationText = "You got a friend request from $request"
                val builder = NotificationCompat.Builder(this.context, "FRIEND")
                    .setSmallIcon(R.drawable.ic_friends_request)
                    .setContentTitle("Friend request")
                    .setContentText(notificationText)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(notificationText)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "friend"
                    val descriptionText = "friend request"
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel =
                        NotificationChannel("FRIEND", name, importance).apply {
                            description = descriptionText
                        }

                    val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                with(NotificationManagerCompat.from(context)) {
                    notify(Random.nextInt(), builder.build())
                }
            }
        }

        val matchRequests = getMatchRequests(context)

        if (matchRequests != null) {
            for (request in matchRequests) {
                val notificationText = "You got a match request from ${request.username}"
                val builder = NotificationCompat.Builder(this.context, "MATCH")
                    .setSmallIcon(R.drawable.ic_match_requests)
                    .setContentTitle("Match request")
                    .setContentText(notificationText)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(notificationText)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "match"
                    val descriptionText = "match request"
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel =
                        NotificationChannel("MATCH", name, importance).apply {
                            description = descriptionText
                        }

                    val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                with(NotificationManagerCompat.from(context)) {
                    notify(request.id.toInt(), builder.build())
                }
            }
        }

        return Result.success()
    }
}