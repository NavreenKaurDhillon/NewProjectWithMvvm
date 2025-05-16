package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.base.BaseApplication
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.ActivityChatBinding
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.ItemMessageDialogBinding
import com.live.humanmesh.databinding.MenuLayoutBinding
import com.live.humanmesh.databinding.ReportBottomSheetBinding
import com.live.humanmesh.model.ImageUploadResponse
import com.live.humanmesh.model.Message
import com.live.humanmesh.model.MessageListResponse
import com.live.humanmesh.model.SendMessageResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.maskString
import com.live.humanmesh.utils.AppUtils.prepareMultiPart
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.imageupload.ImagePickerUtilityFragment
import com.live.humanmesh.utils.Progress
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.utils.socket.SocketManager
import com.live.humanmesh.utils.socket.SocketManager.Observer
import com.live.humanmesh.view.activity.FullScreenImage
import com.live.humanmesh.view.adapter.MessageAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class UserChatFragment : ImagePickerUtilityFragment(), Observer,
    androidx.lifecycle.Observer<Resource<JsonElement>> {

    private lateinit var binding: ActivityChatBinding
    private lateinit var socketManager: SocketManager
    private var adapter: MessageAdapter? = null
    private var receiverId = ""
    private var accountPublic = ""
    private var photoVisibility = ""
    private var profileVisitPermission = ""
    private var blockedByMe = ""
    private var blockedByOther = ""
    private val messageList = ArrayList<Message>()
    private var isPopupShown = false
    private var isPhotoPermissionAccepted = false
    private lateinit var appViewModel: AppViewModel
    private var receiverGender: String? = null
    private var totalWords = 0
    private var remainingWords = 0

    companion object {
        var lastDate = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socketManager = BaseApplication.context?.getSocketMySocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        if (arguments != null)
            if (requireArguments().getString("receiverId", "") != null)
                receiverId = requireArguments().getString("receiverId", "")
        Progress.show(requireContext())
        Constants.isOnChat = true
        getChatList()

//        setChatsAdapter(false)
        setClickListeners()
    }


    private fun getChatList() {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        socketManager.getChat(jsonObject)
    }

    @SuppressLint("NewApi")
    private fun setClickListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivMenu.setOnClickListener {
                showPopUp(it)
            }
            ivUserView.setOnClickListener {
                if (accountPublic == "1") {
                    //show profile directly
                    val bundle = Bundle()
                    bundle.putString("userId", receiverId)
                    bundle.putString("profileVisitPermission", profileVisitPermission)
                    findNavController().navigate(R.id.userDetailFragment, bundle)
                } else
                    showProfileViewDialog()
            }
            btnSend.setOnClickListener {
                remainingWords?.let { it1 ->
                    if (it1 > 0)
                        sendMessage()
                    else
                        showErrorAlert(getString(R.string.you_have_no_words_left))
                }
            }
            ivGallery.setOnClickListener {
                if (photoVisibility !="1")
                    showPhotosPrivacyPopup("Gallery")
                else
                    getImage()
            }


        }
    }
    private fun addTextInput(){
        binding.messageET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val currentLength = editable?.length ?: 0
                val remainingWords = remainingWords - currentLength
                setCount(remainingWords.coerceAtLeast(0)) // Prevent negative numbers
                binding.messageET.filters = arrayOf(InputFilter.LengthFilter(remainingWords))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setCount(count: Int) {
        binding.wordsCountTV.text = getString(
            R.string.you_have_words_left,
            count.toString()
        )
        if (count > 20)
            binding.wordsCountTV.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green_color
                )
            )
        else
            binding.wordsCountTV.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red_color
                )
            )

    }

    private fun sendMessage() {
        val jsonObject = JSONObject()
//        message, senderId, receiverId, msgType, thumbnail
        jsonObject.put("message", binding.messageET.text.toString().trim())
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        jsonObject.put("totalWords", binding.messageET.text.toString().count().toString())
        jsonObject.put("msgType", "1")  //0=>text,1=>image,2 => thumbnail
        socketManager.sendMessage(jsonObject)
        Progress.show(requireContext())
    }

    private fun sendImage(thumbnail: String) {
        val jsonObject = JSONObject()
//      message, senderId, receiverId, msgType, thumbnail
        jsonObject.put("message", "")
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        jsonObject.put("thumbnail", thumbnail)
        jsonObject.put("totalWords", "1")
        jsonObject.put("msgType", "2")  //0=>text,1=>image,2 => thumbnail
        Log.d("Socketdfjewgfjw", "sendImage: $jsonObject")
        socketManager.sendMessage(jsonObject)
    }


    private fun setChatsAdapter(msgList: ArrayList<Message>, receiverGender: String?) {
        adapter = MessageAdapter(requireContext(), msgList, receiverGender)
        binding.rvChat.adapter = adapter
        adapter?.clickListener = { type, value ->
            when (type) {
                "details" -> {
                    val bundle = Bundle()
                    bundle.putString("userId", value)
                    bundle.putString("profileVisitPermission", profileVisitPermission)
                    findNavController().navigate(R.id.userDetailFragment, bundle)
                }
                "image" -> {
                    startActivity(
                        Intent(requireActivity(), FullScreenImage::class.java)
                            .putExtra("imageUrl", value)
                    )
                }
            }

        }
    }

    private fun showPopUp(view: View) {
        val binding = MenuLayoutBinding.inflate(LayoutInflater.from(requireContext()))

        val popupWindow = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (blockedByMe == "1")
            binding.tvBlock.text = getString(R.string.unblock)
        else
            binding.tvBlock.text = getString(R.string.block)

        binding.tvReport.setOnClickListener {
            showReportDialog()
            popupWindow.dismiss()
        }
        binding.tvClearChat.setOnClickListener {
            showClearChatDialog()
            popupWindow.dismiss()
        }
        binding.tvBlock.setOnClickListener {
            showBlockDialog()
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        binding.root.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(view)
    }

    @SuppressLint("NewApi")
    private fun showPhotosPrivacyPopup(from: String) {
        val dialogBinding = ItemMessageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.CENTER
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        with(dialogBinding) {
            btnYes.setOnClickListener {
                dialog.dismiss()
                if (from == "Home")
                    requestPermissionFromUser("1")
                else
                    requestPermissionFromUser("1")
//                    getImage()
//                setChatsAdapter(true)
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }
        isPopupShown = true
        dialog.show()
    }

    private fun showProfileViewDialog() {
        val dialogBinding = CommonBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        with(dialogBinding) {
            ivTop.setImageResource(R.drawable.user_view)
            tvHeading.text = getString(R.string.view_profile)
            tvSubHeading.text =
                getString(R.string.do_you_want_to_show_you_profile_detail_to_this_user)
            btnYes.setOnClickListener {
                dialog.dismiss()
                requestPermissionFromUser("2")
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun requestPermissionFromUser(requestType: String) {
        /* permissionRequest["senderId":Store.userDetails?.id ?? 0,
         "receiverId":receiver_id,"type":type]
         2-> viewprofile, 1: image permission*/

        val jsonObject = JSONObject()
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        jsonObject.put("type", requestType)
        socketManager.requestPermission(jsonObject)
    }


    private fun showReportDialog() {
        val dialogBinding = ReportBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogBinding.btnYes.setOnClickListener {
            dialog.dismiss()
            reportUser(dialogBinding.reportMessage.text.toString().trim())
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showClearChatDialog() {
        val dialogBinding = CommonBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        with(dialogBinding) {
            ivTop.setImageResource(R.drawable.img_trash)
            tvHeading.text = getString(R.string.clear_chat)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_clear_this_chat)
            btnYes.setOnClickListener {
                dialog.dismiss()
                clearChat()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showBlockDialog() {
        val dialogBinding = CommonBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)
        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        with(dialogBinding) {
            ivTop.setImageResource(R.drawable.img_blocked)
            if (blockedByMe == "1")
                tvHeading.text = getString(R.string.unblock)
            else
                tvHeading.text = getString(R.string.block)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_block_this_user)
            btnYes.setOnClickListener {
                dialog.dismiss()
                blockUnblockUser()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun blockUnblockUser() {
        val jsonObject = JSONObject()
        jsonObject.put("block_by", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("block_to", receiverId)
        jsonObject.put("reason", "")
        if (blockedByMe == "1")
            jsonObject.put("status", "0")  //1 for block 0 unlock
        else
            jsonObject.put("status", "1")
        socketManager.blockUser(jsonObject)
    }

    override fun onError(event: String, vararg args: Any) {
    }

    override fun onResponse(event: String, vararg args: Any) {
        Log.d("Socketdfdfd", "onResponse:  user fraggg ==== $event ${args[0]}")
        when (event) {
            SocketManager.SEND_MESSAGE_LISTNER -> {
                CoroutineScope(Dispatchers.Main).launch {
                    Progress.hide()
                    binding.messageET.clearFocus()
                    binding.messageET.setText("")
                    val data = args[0] as JSONObject
                    val model = Gson().fromJson(data.toString(), SendMessageResponse::class.java)
                    if (model.getMsgs != null) {
                        if (messageList.isEmpty())
                            setChatsAdapter(messageList, receiverGender)
                        messageList.add(
                            Message(
                                createdAt = model.getMsgs.createdAt,
                                senderId = model.getMsgs.senderId,
                                receiverId = model.getMsgs.receiverId,
                                message = model.getMsgs.message,
                                msgType = model.getMsgs.msgType,
                                thumbnail = model.getMsgs.thumbnail,
                                senderImage = model.getMsgs.senderImage,
                                senderName = model.getMsgs.senderName,
                                receiverImage = model.getMsgs.receiverImage,
                                receiverName = model.getMsgs.receiverName,
                            )
                        )
                        Log.d("bjhjb", "onResponse: $messageList")
                        binding.noDataTV.gone()
                        binding.rvChat.visible()
                        adapter?.notifyItemInserted(messageList.size - 1)
                        binding.rvChat.scrollToPosition(messageList.size - 1)
                        Log.d("ewnmjen", "onResponse: send ${model.remaining_words}")
                    }
                    remainingWords = model.remaining_words
                    setCount(remainingWords)
                    if (model.status == 402) {
                        showErrorAlert(model.message.toString())
                    }
                    Log.d("gjjhgwqhgdgh", "onResponse: $totalWords  $remainingWords")
                }
            }

            SocketManager.GET_CHAT_LISTNER_ONE_TO_ONE -> {
                CoroutineScope(Dispatchers.Main).launch {

                    Progress.hide()
                    binding.messageET.clearFocus()
                    binding.messageET.setText("")
                    val data = args[0] as JSONObject
                    val model = Gson().fromJson(data.toString(), MessageListResponse::class.java)
                    Log.d(
                        "Socketdf",
                        "onResponse: GET_CHAT_LISTNER_ONE_TO_ONE size = ${model.messageList.size} "
                    )
                    Log.d("ewnmjen", "onResponse: chat ${model.remaining_words}")
                    if (model.receiverDetails != null) {
                        receiverGender = model.receiverDetails.gender
                        if (accountPublic == "1" || profileVisitPermission == "1")
                            binding.tvHeading.text = model.receiverDetails?.name
                        else
                            binding.tvHeading.text = maskString(model.receiverDetails?.name.toString())
                        accountPublic = model.receiverDetails.account_public
                        photoVisibility = model.receiverDetails.photo_visiblity
                    }
                    if (model.messageList.isEmpty()) {
                        binding.noDataTV.visible()
                    } else {
                        binding.rvChat.visible()
                        binding.noDataTV.gone()
                        if (messageList.isNotEmpty())
                            messageList.clear()
                        messageList.addAll(model.messageList)
                        setChatsAdapter(messageList, receiverGender)
                        binding.rvChat.scrollToPosition(messageList.size - 1)
                    }
                    setCount(model.remaining_words)
                    blockedByMe = model.isBlockByMe.toString()
                    blockedByOther = model.isBlockByOther.toString()
                    remainingWords = model.remaining_words
                    addTextInput()
                    profileVisitPermission = model.profileVisitPermission.toString()
                    if (accountPublic == "1" || profileVisitPermission == "1")
                        binding.ivUserView.gone()
                    if (blockedByMe == "1" || blockedByOther == "1")
                        binding.ivUserView.isEnabled = false
                    if (model.isBlockByMe == 1)
                        showBlocked(1)
                    if (model.isBlockByOther == 1)
                        showBlocked(2)

                    /*  if (!isPopupShown && mo
                    el.isBlockByMe!=1)
                          showPhotosPrivacyPopup("Home")*/
                }
            }

            SocketManager.BLOCK_DATA -> {
                val data = args[0] as JSONObject
                val message = data.getString("success_msg")
                CoroutineScope(Dispatchers.Main).launch {
                    showSuccessAlert(message)
                    blockedByMe = data.getString("isBlockByMe").toString()
                    blockedByOther = data.getString("isBlockByOther").toString()
                    if (blockedByMe == "1")
                        showBlocked(1)
                    else if (blockedByOther == "1")
                        showBlocked(2)
                    else {
                        binding.blockTV.gone()
                        binding.messageET.setText("")
                        binding.relativeLayout2.visible()
                    }
                }
            }

            SocketManager.REQUEST_PERMISSION_LISTENER -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val data = args[0] as JSONObject
                    val message = data.getString("message")
                    showSuccessAlert(message)
                }
            }

            SocketManager.DELETE_LISTENER -> {
                CoroutineScope(Dispatchers.Main).launch {
                    messageList.clear()
                    adapter?.notifyDataSetChanged()
                    binding.rvChat.gone()
                    binding.noDataTV.visible()
                }
            }

            SocketManager.REPORT_USER_LISTENER -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val data = args[0] as JSONObject
                    val message = data.getString("success_msg")
                    Progress.hide()
                    showSuccessAlert(message)
//                  showSuccessAlert("User reported successfully")
                }
            }
        }
    }

    private fun showBlocked(type: Int) {
        // 1- by me, 2 - by other
        binding.blockTV.visible()
        binding.relativeLayout2.gone()
        if (type == 1)
            binding.blockTV.text = getString(R.string.you_have_blocked_this_user)
        else
            binding.blockTV.text = getString(R.string.you_have_been_blocked_by_this_user)
    }

    private fun clearChat() {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("receiverId", receiverId)
        socketManager.clearChat(jsonObject)
    }

    private fun reportUser(reason: String) {
        Progress.show(requireContext())
        val jsonObject = JSONObject()
        jsonObject.put("reported_by", getFromDatabase(Constants.USER_ID, ""))
        jsonObject.put("reported_to", receiverId)
        jsonObject.put("reason", reason)
        socketManager.reportUser(jsonObject)
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

    override fun selectedImage(imagePath: String?, code: Int, type: String) {
        uploadImage(imagePath)
    }

    private fun uploadImage(imagePath: String?) {
        if (imagePath != "") {
            val image = prepareMultiPart("image", File(imagePath))
            appViewModel.postMultipart(Constants.IMAGE_UPLOAD, requireContext(), true, image)
                .observe(viewLifecycleOwner, this)
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.IMAGE_UPLOAD) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        ImageUploadResponse::class.java
                    )
                    if (result.code == 200) {
                        sendImage(result.body.image)
                    }
                }
            }

            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }

            else -> {}
        }
    }

}