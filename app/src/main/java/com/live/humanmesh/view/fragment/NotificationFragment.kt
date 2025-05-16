package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.view.adapter.NotificationsAdapter
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.base.BaseApplication
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.FragmentNotificationBinding
import com.live.humanmesh.model.AllChatsResponse
import com.live.humanmesh.model.AllChatsResponseItem
import com.live.humanmesh.model.Notification
import com.live.humanmesh.model.NotificationsListResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.NOTIFICATION_LIST
import com.live.humanmesh.utils.Progress
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.utils.socket.SocketManager
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@AndroidEntryPoint
class NotificationFragment : Fragment(), Observer<Resource<JsonElement>>, SocketManager.Observer {

    var binding: FragmentNotificationBinding? = null
    private lateinit var appViewModel: AppViewModel
    private lateinit var socketManager: SocketManager
    private var receiverId = ""
    private var notificationId = ""
    private var notificationType = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        socketManager = BaseApplication.context?.getSocketMySocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }
        hitGetListing()
        initClickListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initClickListener() {
        binding!!.apply {
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                if (!findNavController().popBackStack()) {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
            toolBar.tvTitle.text = getString(R.string.notifications)
        }
    }

    private fun hitGetListing() {
        appViewModel.getApi(NOTIFICATION_LIST, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun setAdapter(notifications: List<Notification>) {
        val adapter = NotificationsAdapter(requireActivity(), notifications)
        binding?.rvList?.adapter = adapter
        adapter.clickListener = { case, notId, senderId ->
            notificationId = notId
            receiverId = senderId
            if (case == "accept")
                updateRequestStatus("1")
            else
                updateRequestStatus("2")
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == NOTIFICATION_LIST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        NotificationsListResponse::class.java
                    )
                    if (result.code == 200)
                        if (result.body.notifications.isNotEmpty()) {
                            setAdapter(result.body.notifications)
                            binding?.rvList?.visible()
                        }
                        else {
                            binding?.noDataTV?.visible()
                            binding?.rvList?.gone()
                        }
                }
            }

            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }

            else -> {}
        }
    }

    private fun updateRequestStatus(status: String) {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        jsonObject.put("type", notificationType)
        jsonObject.put("status", status)
        jsonObject.put("notification_id", notificationId)
        socketManager.updateRequestStatus(jsonObject)
    }

    override fun onResume() {
        super.onResume()
        socketManager.unRegister(this)
        socketManager.onRegister(this)
    }

    override fun onDestroy() {
        Constants.isOnChat = false
        socketManager?.unRegister(this)
        super.onDestroy()

    }

    override fun onError(event: String, vararg args: Any) {

    }

    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.REQUEST_STATUS_LISTENER -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val data = args[0] as JSONObject
                    val message = data.getString("message")
                    showSuccessAlert(message)
                    hitGetListing()
                }
            }
        }
    }


    class NotificationModel(val title: String, val des: String)
}