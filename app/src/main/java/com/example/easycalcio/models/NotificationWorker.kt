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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun runInstantWorker(context: Context) {
    val chatNotificationWorker = OneTimeWorkRequestBuilder<ChatNotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(chatNotificationWorker)
    val matchRequestNotificationWorker = OneTimeWorkRequestBuilder<MatchRequestNotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(matchRequestNotificationWorker)
    val friendRequestNotificationWorker = OneTimeWorkRequestBuilder<FriendRequestNotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(friendRequestNotificationWorker)
    startWorker(context)
}

fun startWorker(context: Context) {
    val dbRef = FirebaseDbWrapper(context).dbRef
    GlobalScope.launch {
        dbRef.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatNotificationWorker = OneTimeWorkRequestBuilder<ChatNotificationWorker>().build()
                WorkManager.getInstance(context).enqueue(chatNotificationWorker)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    GlobalScope.launch {
        dbRef.child("friendRequests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchRequestNotificationWorker = OneTimeWorkRequestBuilder<MatchRequestNotificationWorker>().build()
                WorkManager.getInstance(context).enqueue(matchRequestNotificationWorker)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    GlobalScope.launch {
        dbRef.child("friendRequests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendRequestNotificationWorker = OneTimeWorkRequestBuilder<FriendRequestNotificationWorker>().build()
                WorkManager.getInstance(context).enqueue(friendRequestNotificationWorker)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class MatchRequestNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
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

class FriendRequestNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        val friendRequests = getReceivedRequests(context)

        if (friendRequests != null) {
            for (request in friendRequests) {
                val notificationText = "You got a friend request from ${request.username}"
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
        return Result.success()
    }

}

class ChatNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {

        val chats = getChats(context)
        val user = getUser(context)

        if (chats != null) {
            for (chat in chats) {
                val messages = chat.messages
                if (messages != null) {
                    for (message in messages) {
                        if (message.sender != user.username && !message.read) {
                            val notificationText = "You got a new message from ${message.sender}"
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
        return Result.success()
    }
}