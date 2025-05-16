package com.live.humanmesh.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.live.humanmesh.R
import com.live.humanmesh.base.BaseApplication
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.FragmentChatBinding
import com.live.humanmesh.model.AllChatsResponse
import com.live.humanmesh.model.AllChatsResponseItem
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Progress
import com.live.humanmesh.utils.socket.SocketManager
import com.live.humanmesh.utils.socket.SocketManager.Observer
import com.live.humanmesh.view.adapter.ChatsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class ChatFragment : Fragment(), Observer {
    private lateinit var binding : FragmentChatBinding
    private lateinit var adapter: ChatsAdapter
    private var socketManager: SocketManager?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBar.ivBack.gone()
        binding.toolBar.tvTitle.text= getString(R.string.chat)
        Progress.show(requireContext())
        socketManager = BaseApplication.context?.getSocketMySocketManager()!!
        if (socketManager?.isConnected() == false || socketManager?.getmSocket() == null) {
            socketManager?.init()
        }
        getChatList()
        clickListeners()
    }

    private fun getChatList() {
        val jsonObject = JSONObject()
        jsonObject.put("userId", getFromDatabase(Constants.USER_ID, ""))
        socketManager?.getAllChats(jsonObject)
    }

    private fun clickListeners() {
        binding.apply {
        }
    }

    private fun setChatsAdapter(chats: java.util.ArrayList<AllChatsResponseItem>) {
       adapter = ChatsAdapter(chats)
        binding.chatsRV.adapter = adapter
        adapter.clickListener={
            Log.d("wlfhekjwe", "setChatsAdapter: chat selected id = $it")
            val bundle = Bundle()
            bundle.putString("receiverId",it)
            findNavController().navigate(R.id.chat_fragment_to_user_chat, bundle)
        }
    }

    override fun onError(event: String, vararg args: Any) {

    }

    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.CHAT_LISTING_LISTNER -> {
                val data = args[0] as JSONArray
                val model = Gson().fromJson(data.toString(), AllChatsResponse::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                if (model.isNotEmpty()){
                    val chats = ArrayList<AllChatsResponseItem>()
                    chats.addAll(model)
                        Progress.hide()
                        setChatsAdapter(chats.filter { it.receiverName!="" } as java.util.ArrayList<AllChatsResponseItem>)
                    }
                else{
                    Progress.hide()
                    binding.noDataTV.visible()
                }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        socketManager?.unRegister(this)
        socketManager?.onRegister(this)
    }
    override fun onDestroy() {
        Constants.isOnChat = false
        socketManager?.unRegister(this)
        super.onDestroy()

    }



}