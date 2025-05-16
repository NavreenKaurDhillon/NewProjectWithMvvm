package com.live.humanmesh.utils.firebase

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.live.humanmesh.view.activity.MainActivity
import com.live.humanmesh.R
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.view.activity.HomeActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var i = 0
    private var title = ""
    private var type = ""
    private var message: String? = ""
    private var CHANNEL_ID = "humanmesh"
    private lateinit var soundUri: Uri

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        Log.e("wekfjhjkwe", "Refreshed token: $refreshedToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
/*
        {type=2, title=You got a message from s****d, event_id=, senderImage=, message=You got a message from s****d, senderName=sadasd, senderId=35}
*/
        Log.d("FireBasePush", "Notification: ${remoteMessage}")
        Log.d("FireBasePush", "noti=  ${remoteMessage.notification}")
        Log.d("FireBasePush", "Notification data : ${remoteMessage.data}")
        Log.d("FireBasePush", "Notification data TITLE : ${remoteMessage.data["title"]}")
        Log.d("FireBasePush", "Notification data type  : ${remoteMessage.data["type"]}")

        try {
            if (remoteMessage.data["title"]?.isNotEmpty() == true){
                title = remoteMessage.data["title"].toString()
                message = remoteMessage.data["message"]
                type = remoteMessage.data["type"].toString()
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("type",type)
//            "1" -> send photo and profileview request (redirect to notificaton scrreen)
//            "2" and "3" - > Chat push(2), 3 ke case mein bhi chat pr bhej dena
//            "4"-> new request which is shown on home(redirect to homedetail screen)
//            "5"-> when user accept home request (redirect to matched user screen on 2nd tab)
            when (type) {
                "1" -> {
                   makePush(intent)
                }
                "2" -> { //chat
//                    {type=2, title=New Message, event_id=, senderImage=, message=You got a message from s****d, senderName=sadasd, senderId=35}
                    if (!Constants.isOnChat) {
                        val senderId = remoteMessage.data["senderId"]
                        intent.putExtra("senderId", senderId.toString())
                        makePush(intent)
                    }
                }
                "4" -> {
//                    {type=4, title=New Message, event_id=66, senderImage=, message=You have a new request., senderName=jujhar, senderId=34}
                    intent.putExtra("eventId",  remoteMessage.data["event_id"].toString())
                    makePush(intent)
                }
                "5" -> {
                    makePush(intent)
                }
                else -> {
//                    {type=6, title=New Message, event_id=0, senderImage=16e9e971-46a5-4734-8a63-9e974c7b9b2a.jpeg, message=Admin sent you a message., senderName=Adam, senderId=1}
                    makePush(intent)
                }
            }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun makePush(intent: Intent?) {
        //foreground handling
        val pendingIntent = PendingIntent.getActivity(
            this, i,
            intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        intent?.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val channelId = CHANNEL_ID
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(notificationIcon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.MAGENTA
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private val notificationIcon: Int
        @SuppressLint("ObsoleteSdkInt")
        get() {
            val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
        }
}