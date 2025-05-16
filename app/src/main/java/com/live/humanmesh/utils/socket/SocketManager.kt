package com.live.humanmesh.utils.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

public class SocketManager {

    companion object {
        // sockets events
        const val CONNECT_USER = "connect_user"
        const val SEND_MESSAGE = "send_message"
        const val GET_CHAT_EMITTER = "message_list"
        const val CHAT_LISTING_EMITTER = "chat_users_list" //chat with all listing
        const val DELETE_EMITTER = "clear_chat"
        const val BLOCK_UNBLOCK_USER = "block_user"
        const val REPORT_USER = "reportEmitter"
        const val REQUEST_PERMISSION = "permissionRequest"
        const val REQUEST_STATUS = "requestStatus"

        /*  //video call
            const val CALL_TO_USER = "callToUser"
            const val CALL_TO_RECEIVER = "callToReciever"
            const val CALL_STATUS = "callStatus"
            const val GET_READ_UNREAD_EMITTER = "read_unread"
            const val CHECK_BLOCK_USER = "check_block_user"
        */

        //listener
        const val CONNECT_LISTENER = "connect_user"
        const val SEND_MESSAGE_LISTNER = "send_message_emit" // send message
        const val GET_CHAT_LISTNER_ONE_TO_ONE = "message_list" // one to one chat
        const val CHAT_LISTING_LISTNER = "chat_users_list" //chat with all listing outside
        const val CHECK_BLOCK_USER_LISTENER = "blockListener"
        const val REPORT_USER_LISTENER = "reportListener"
        const val BLOCK_DATA = "blockListener"
        const val DELETE_LISTENER = "clear_chat_listener"
        const val REQUEST_PERMISSION_LISTENER = "permissionRequest"
        const val REQUEST_STATUS_LISTENER = "requestStatus"


        /*  //video call listener
          const val CALL_TO_USER_LISTENER = "call_to_user"
          const val CALL_TO_RECEIVER_LISTENER = "call_to_reciever"
          const val CALL_STATUS_LISTENER = "call_status"
          */
    }

    var t = 0
    private var mSocket: Socket? = null
    private var observerList: MutableList<Observer>? = null

    fun isConnected(): Boolean {
        return mSocket != null && mSocket!!.connected()
    }

    fun getmSocket(): Socket? {
        return mSocket
    }


    private fun getSocket(): Socket? {
        run {
            try {
                mSocket = IO.socket(Constants.SOCKET_URL)
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
        return mSocket
    }

    fun init() {
        initializeSocket()
    }

    fun getAllChats(jsonObject: JSONObject) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.emit(CHAT_LISTING_EMITTER, jsonObject)
            mSocket!!.off(CHAT_LISTING_LISTNER)
            mSocket!!.on(CHAT_LISTING_LISTNER, onGetChatmessages)
        } else {
            mSocket!!.emit(CHAT_LISTING_EMITTER, jsonObject)
            mSocket!!.on(CHAT_LISTING_LISTNER, onGetChatmessages)
        }
    }
     fun updateRequestStatus(jsonObject: JSONObject){
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.emit(REQUEST_STATUS, jsonObject)
            mSocket!!.off(REQUEST_STATUS_LISTENER)
            mSocket!!.on(REQUEST_STATUS_LISTENER, requestStatusListener)
        } else {
            mSocket!!.emit(REQUEST_STATUS, jsonObject)
            mSocket!!.on(REQUEST_STATUS_LISTENER, requestStatusListener)
        }
    }

    fun requestPermission(jsonObject: JSONObject){
        Log.d("Socketdfdf", "requestPermission: $jsonObject")
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.emit(REQUEST_PERMISSION, jsonObject)
            mSocket!!.off(REQUEST_PERMISSION_LISTENER)
            mSocket!!.on(REQUEST_PERMISSION_LISTENER, onGetPermissionListener)
        }
        else {
            mSocket!!.emit(REQUEST_PERMISSION, jsonObject)
            mSocket!!.on(REQUEST_PERMISSION_LISTENER, onGetPermissionListener)
        }
    }


    private val onGetChatmessages = Emitter.Listener { args ->
        Log.d("Socketdfdfd", "CHAT_LISTING_LISTNER  ${args[0]}")
        for (observerrr in observerList!!) {
            observerrr.onResponse(CHAT_LISTING_LISTNER, args.get(0))
        }
    }

    private val requestStatusListener = Emitter.Listener { args ->
        Log.d("Socketdfdfd", "requestStatusListener out side ${args[0]}")
        for (observerrr in observerList!!) {
            observerrr.onResponse(REQUEST_STATUS_LISTENER, args.get(0))
        }
    }

    private val onGetPermissionListener = Emitter.Listener { args ->
        Log.d("Socketdfdfd", "onGetPermissionListener ${args[0]}")
        for (observerrr in observerList!!) {
            observerrr.onResponse(REQUEST_PERMISSION_LISTENER, args[0])
        }
    }

    fun getChat(jsonObject: JSONObject?) { // in sider chat
        Log.d("Socketdfdfd", "onGetChatListener000  $jsonObject")
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.emit(GET_CHAT_EMITTER, jsonObject)
            mSocket!!.off(GET_CHAT_LISTNER_ONE_TO_ONE)
            mSocket!!.on(GET_CHAT_LISTNER_ONE_TO_ONE, onGetChatListListener)
            mSocket!!.off(SEND_MESSAGE_LISTNER)
            mSocket!!.on(SEND_MESSAGE_LISTNER, onBodyListener)
        } else {
            mSocket!!.emit(GET_CHAT_EMITTER, jsonObject)
            mSocket!!.on(GET_CHAT_LISTNER_ONE_TO_ONE, onGetChatListListener)
            mSocket!!.off(SEND_MESSAGE_LISTNER)
            mSocket!!.on(SEND_MESSAGE_LISTNER, onBodyListener)
        }
    }

    private val onGetChatListListener = Emitter.Listener { args ->  //one to one chat
        Log.e("Socketdfdfd", "onGetChatListListener $args")
        try {
            for (observerlist in observerList!!) {
                observerlist.onResponse(GET_CHAT_LISTNER_ONE_TO_ONE, args[0])
            }
        } catch (e: Exception) {
            Log.d("erroorrrrr", e.localizedMessage)
        }
    }

    private val onConnectResponce = Emitter.Listener { args ->
        Log.e("Socketdfdfd", "===ConnectTEDww==")
        for (observerlist in observerList!!) {
            Log.e("Socketdfdfd", "===ConnectTED==")
            // no need for call
            // observerlist.onResponse(GET_READDATA_LISTNER, args)
        }
    }

    private val onConnect = Emitter.Listener {
        Log.e("Socketdfdfd", "===onConnect==")

        if (isConnected()) {
            try {
                val jsonObject = JSONObject()

                var userId = ""
                //   val model = getUserModel(AppController.instance!!.applicationContext)
                if (getFromDatabase(Constants.USER_ID, "").isNotEmpty()) {
                    userId = getFromDatabase(Constants.USER_ID, "")
                }

                val userid = userId
                //  val userid = HomeActivity.loginUserId

                if (userid.isNotEmpty()) {
                    if (userid.toInt() != 0) {
                        jsonObject.put("userId", userid.toInt())
                        mSocket!!.off(CONNECT_LISTENER)
                        mSocket!!.on(CONNECT_LISTENER, onConnectResponce)
                        Handler(Looper.getMainLooper()).post {
                            mSocket!!.emit(CONNECT_USER, jsonObject)
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            initializeSocket()
        }
    }

    private fun disconnect() {
        if (mSocket != null) {
            // mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            // mSocket!!.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)

            mSocket!!.off(SEND_MESSAGE_LISTNER, onBodyListener)
            mSocket!!.off(CHAT_LISTING_LISTNER, onGetChatmessages)
            mSocket!!.off(GET_CHAT_LISTNER_ONE_TO_ONE, onGetChatListListener)
            mSocket!!.off(REPORT_USER_LISTENER, onReportUserListener)
             mSocket!!.off(BLOCK_DATA, onBlockListener)
            mSocket!!.off(DELETE_LISTENER, onDeleteChatListener)

            mSocket!!.off()
            mSocket!!.disconnect()
        }
    }

    fun disconnectAll() {
        Log.e("Socketdfdfd", "Destroyyy")
        if (mSocket != null) {
            mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            //     mSocket!!.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)

            mSocket!!.off(SEND_MESSAGE_LISTNER, onBodyListener)
//            mSocket!!.off(CHAT_LISTING_LISTNER, onGetChatmessages)
            mSocket!!.off(GET_CHAT_LISTNER_ONE_TO_ONE, onGetChatListListener)
            mSocket!!.off()
        }
    }

    fun onRegister(observer: Observer) {
        if (observerList != null && !observerList!!.contains(observer)) {
            observerList!!.clear()
            observerList!!.add(observer)
        } else {
            observerList = ArrayList()
            observerList!!.clear()
            observerList!!.add(observer)
        }
    }

    fun unRegister(observer: Observer) {
        observerList?.let { list ->
            for (i in 0 until list.size - 1) {
                val model = list[i]
                if (model == observer) {
                    observerList?.remove(model)
                }
            }
        }
    }

    private val onDisconnect = Emitter.Listener { args ->
        mSocket!!.connect()
        for (observer in observerList!!) {
            observer.onError("errorSocket", args)
        }
        Log.e("Socketdfdfd", "DISCONNECTED")
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.e("Socketdfdfd", "CONNECTION ERROR")
        mSocket!!.connect()
        for (observer in observerList!!) {
            observer.onError("errorSocket", args)
        }
        // Delay reconnection attempt to avoid tight loop
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mSocket!!.connected()) {
                Log.e("Socket", "Retrying socket connection...")
                mSocket!!.connect()
            }
        }, 2000) // retry after 3 seconds
    }

    fun sendMessage(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(SEND_MESSAGE_LISTNER)
            mSocket!!.on(SEND_MESSAGE_LISTNER, onBodyListener)
            mSocket!!.emit(SEND_MESSAGE, jsonObject)
            Log.d("Socketdfdfd", "SEND_MESSAGE : $jsonObject")
        } else {
            mSocket!!.off(SEND_MESSAGE_LISTNER)
            mSocket!!.on(SEND_MESSAGE_LISTNER, onBodyListener)
            mSocket!!.emit(SEND_MESSAGE, jsonObject)

            Log.d("Socketdfdfd", jsonObject.toString())
        }
    }

    private val onBodyListener = Emitter.Listener { args ->
        Log.d("Socketdfdfdeee   send message listener :  ", Gson().toJson(args[0]).toString())
        for (observerr in observerList!!) {
            observerr.onResponse(SEND_MESSAGE_LISTNER, args[0])
        }
    }

    fun blockUser(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()

            mSocket!!.off(BLOCK_DATA)
            mSocket!!.on(BLOCK_DATA, onBlockListener)
            mSocket!!.emit(BLOCK_UNBLOCK_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE00")
        } else {
            mSocket!!.off(BLOCK_DATA)
            mSocket!!.on(BLOCK_DATA, onBlockListener)
            mSocket!!.emit(BLOCK_UNBLOCK_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE")
        }
    }

    private val onBlockListener = Emitter.Listener { args ->
        Log.e("Socketdfdfdeee", Gson().toJson(args[0]).toString())
        for (observerr in observerList!!) {
            Log.e("Socketdfdfdeee", "ONBODY" + "   " + t.toString())
            observerr.onResponse(BLOCK_DATA, args[0])
        }
    }


    // call to user
    /* fun callToUser(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.emit(CALL_TO_USER, jsonObject)
        } else {
            mSocket!!.off(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.emit(CALL_TO_USER, jsonObject)
            Log.d("socketRes", "callToUser: " + jsonObject)
        }
    }*/

    // cal status active
    /* fun callStatusActive() {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CALL_TO_USER_LISTENER, onReceiverCallStatusListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onReceiverCallStatusListener)

        } else {
            mSocket!!.off(CALL_TO_USER_LISTENER, onReceiverCallStatusListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onReceiverCallStatusListener)

        }
    }
*/

    // cal to user active

    /*fun callToUserActivate() {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onCalltoUserListener)

        } else {
            mSocket!!.off(CALL_TO_USER_LISTENER, onCalltoUserListener)
            mSocket!!.on(CALL_TO_USER_LISTENER, onCalltoUserListener)

        }
    }*/


    // call status

    /*fun callStatus(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CALL_STATUS_LISTENER, onCallStatusListener)
            mSocket!!.on(CALL_STATUS_LISTENER, onCallStatusListener)
            mSocket!!.emit(CALL_STATUS, jsonObject)
        } else {
            mSocket!!.off(CALL_STATUS_LISTENER, onCallStatusListener)
            mSocket!!.on(CALL_STATUS_LISTENER, onCallStatusListener)
            mSocket!!.emit(CALL_STATUS, jsonObject)
        }
    }*/


     fun clearChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()

            mSocket!!.off(DELETE_LISTENER)
            mSocket!!.on(DELETE_LISTENER, onDeleteChatListener)
            mSocket!!.emit(DELETE_EMITTER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE00")
        } else {
            mSocket!!.off(DELETE_LISTENER)
            mSocket!!.on(DELETE_LISTENER, onDeleteChatListener)
            mSocket!!.emit(DELETE_EMITTER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE")
        }
    }


    /* private val onCalltoUserListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d("SocketListener", "CallStatusList :::$data")
            for (observer in observerList!!) {
                observer.onResponse(CALL_TO_USER_LISTENER, data)
            }
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }*/

    /* private val onCallStatusListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d("SocketListener", "CallStatusList :::$data")
            for (observer in observerList!!) {
                observer.onResponse(CALL_STATUS_LISTENER, data)
            }
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }*/

    /* private val onReceiverCallStatusListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d("SocketListener", "CallStatusList :::$data")
            for (observer in observerList!!) {
                observer.onResponse(CALL_STATUS_LISTENER, data)
            }
        } catch (ex: Exception) {
            ex.localizedMessage
        }

    }*/

    private val onDeleteChatListener = Emitter.Listener { args ->

        Log.e("Socketdfdfdeee", Gson().toJson(args[0]).toString())
        for (observerr in observerList!!) {
            Log.e("Socketdfdfdeee", "ONBODY" + "   " + t.toString())
            observerr.onResponse(DELETE_LISTENER, args[0])
        }
    }


    fun reportUser(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()

            mSocket!!.off(REPORT_USER_LISTENER)
            mSocket!!.on(REPORT_USER_LISTENER, onReportUserListener)
            mSocket!!.emit(REPORT_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE00")
        } else {
            mSocket!!.off(REPORT_USER_LISTENER)
            mSocket!!.on(REPORT_USER_LISTENER, onReportUserListener)
            mSocket!!.emit(REPORT_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE")
        }
    }

    private val onReportUserListener = Emitter.Listener { args ->
        Log.e("Socketdfdfd", "gggggggggggggggggggggggggggggggggggggggggggg")

        for (observerr in observerList!!) {
            Log.e("Socketdfdfdeee", "ONBODY" + "   " + t.toString())
            observerr.onResponse(REPORT_USER_LISTENER, args[0])
        }
    }


    /*  fun checkBlockUser(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()

            mSocket!!.off(CHECK_BLOCK_USER_LISTENER)
            mSocket!!.on(CHECK_BLOCK_USER_LISTENER, onCheckBlockListener)
            mSocket!!.emit(CHECK_BLOCK_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE00")
        } else {
            mSocket!!.off(CHECK_BLOCK_USER_LISTENER)
            mSocket!!.on(CHECK_BLOCK_USER_LISTENER, onCheckBlockListener)
            mSocket!!.emit(CHECK_BLOCK_USER, jsonObject)

            Log.e("Socketdfdfd", "SEND_MESSAGE")
        }
    }*/

    private val onCheckBlockListener = Emitter.Listener { args ->
        Log.e("Socketdfdfd", "gggggggggggggggggggggggggggggggggggggggggggg")

        for (observerr in observerList!!) {
            Log.e("Socketdfdfdeee", "ONBODY" + "   " + t.toString())
            observerr.onResponse(CHECK_BLOCK_USER_LISTENER, args[0])
        }
    }


    fun initializeSocket() {
        Log.e("Socketdfdfd", "======initializeSocket====")

        if (mSocket == null) {
            mSocket = getSocket()
        }

        if (observerList == null || observerList!!.size == 0) {
            observerList = ArrayList()
        }
        disconnect()

        mSocket!!.connect()
        mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

        /*  mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
          mSocket!!.on(Socket.EVENT_RECONNECT, onConnectError)

  */

        mSocket!!.on(SEND_MESSAGE_LISTNER, onBodyListener)
        mSocket!!.on(CHAT_LISTING_LISTNER, onGetChatmessages)
        mSocket!!.on(GET_CHAT_LISTNER_ONE_TO_ONE, onGetChatListListener)
        mSocket!!.on(REPORT_USER_LISTENER, onReportUserListener)
         mSocket!!.on(BLOCK_DATA, onBlockListener)
        mSocket!!.on(DELETE_LISTENER, onDeleteChatListener)
    }

    interface Observer {
        fun onError(event: String, vararg args: Any)
        fun onResponse(event: String, vararg args: Any)
    }

}




