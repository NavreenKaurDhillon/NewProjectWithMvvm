package com.live.humanmesh.view.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.util.fastCbrt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.FragmentDetailsBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.model.EventDetailResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.maskString
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.ACCEPT_REJECT_REQUEST
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.activity.FullScreenImage
import com.live.humanmesh.view.adapter.TextsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailsFragment : Fragment(),Observer<Resource<JsonElement>> {
    private lateinit var binding : FragmentDetailsBinding
    private var eventId = ""
    private lateinit var appViewModel: AppViewModel
    private var bannerImage =""
    private var receiverId =""
    private var isAccepted = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        binding.toolBar.tvTitle.text = getString(R.string.detail)
        if (arguments!=null)
        {
            if ( requireArguments().getString("from","") == "mine") {
                binding.acceptBT.gone()
                binding.rejectBT.gone()
            }
            if (requireArguments().getString("eventId", "")!=null) {
                eventId = requireArguments().getString("eventId", "")
                getEventDetails(eventId)
            }
        }
//      setTextsAdapter()
        clickListeners()
    }

    private fun getEventDetails(eventId: String) {
        val  hashMap = HashMap<String, String>()
        hashMap["eventId"] = eventId
        appViewModel.postApi(Constants.REQUEST_DETAILS, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
    }


    private fun clickListeners() {
        binding.apply {
            bannerIV.setOnClickListener {
                if (bannerImage.isNullOrEmpty()==false){
                    startActivity(Intent(requireActivity(), FullScreenImage::class.java)
                        .putExtra("imageUrl", bannerImage))
                }
            }
            acceptBT.setOnClickListener {
                acceptBT.visibility = View.GONE
                rejectBT.visibility = View.GONE
                chatBT.visibility = View.VISIBLE
                isAccepted = true
                hitAcceptReject(1, eventId)
            }
            rejectBT.setOnClickListener {
                acceptBT.visibility = View.GONE
                rejectBT.visibility = View.GONE
                rejectUser()

            }
            chatBT.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("receiverId", receiverId )
                findNavController().navigate(R.id.userChatFragment, bundle)
            }
            binding.toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun hitAcceptReject(status: Int, eventId: String) {
        val hashMap = HashMap<String, String>()
        hashMap["status"] = status.toString()
        hashMap["eventId"] = eventId
        appViewModel.postApi(ACCEPT_REJECT_REQUEST, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
    }

    private fun setTextsAdapter() {
        val textsAdapter = TextsAdapter()
        binding.textRV.adapter = textsAdapter

    }

    private fun rejectUser() {
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
            ivTop.setImageResource(R.drawable.reject)
            tvHeading.text = getString(R.string.reject)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_reject_this_request)
            btnYes.setOnClickListener {
                dialog.dismiss()
                hitAcceptReject(2, eventId)
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.REQUEST_DETAILS) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        EventDetailResponse::class.java)

                    binding.apply {
                        Log.d("wqfkhkhqwf", "onChanged: ${result.body}")
                        if (result.body.Request!=null) {
                            val data = result.body.Request
                            Glide.with(requireContext())
                                .load(Constants.IMAGES_BASE_URL + result.body.Request.image)
                                .into(bannerIV)
                            bannerImage = Constants.IMAGES_BASE_URL +result.body.Request.image
                            nameTV.text = data.title
                            descriptionTV.setText(data.description)
                            locationTV.text = data.location
                            receiverId = data.id.toString()
                            if (result.body!=null && result.body.Request!=null
                                && result.body.Request.requestDetails!=null &&
                                result.body.Request.requestDetails.isNotEmpty()) {
                                if (result.body.Request.requestDetails.get(0).status!=null){
                                    if (result.body.Request.requestDetails.get(0).status == 1){
                                        acceptBT.visibility = View.GONE
                                        rejectBT.visibility = View.GONE
                                        chatBT.visibility = View.VISIBLE
                                    }
                                }
                                if (result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name_show == "1")
                                    nickNameTV.text = result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name_show.toString()
                                else
                                    nickNameTV.text = maskString(result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name.toString())
                            }
                            tagsTV.text = data.tag
                            if (data.type == "1"){
                                realTimeTV.text = "Afterwards"
                                dateTV.text = data.start_date.toString()
//                            etDate2.text = data.end_date.toString()
                                timeTV.text = data.start_time.toString()+" - "+data.end_time.toString()
                            }
                            else {
                                dateTV.gone()
                                timeTV.gone()
                                clockIV.gone()
                                calendarIV.gone()
                                realTimeTV.text = "Real Time"
                            }
                        }
                        else {
                            val data = result.body
                            Glide.with(requireContext())
                                .load(Constants.IMAGES_BASE_URL + result.body.image).into(bannerIV)
                            bannerImage = Constants.IMAGES_BASE_URL +result.body.image
                            nameTV.text = data.title
                            descriptionTV.setText(data.description)
                            locationTV.text = data.location
                            receiverId = data.id.toString()
                            if (result.body!=null && result.body.Request!=null && result.body.Request.requestDetails!=null && result.body.Request.requestDetails.isNotEmpty()) {
                                if (result.body.Request.requestDetails.get(0).status!=null){
                                    if (result.body.Request.requestDetails.get(0).status == 1){
                                        acceptBT.visibility = View.GONE
                                        rejectBT.visibility = View.GONE
                                        chatBT.visibility = View.VISIBLE
                                    }
                                }
                                if (result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name_show == "1")
                                    nickNameTV.text =
                                        result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name.toString()
                                else
                                    nickNameTV.text =
                                        maskString(result.body.Request.requestDetails?.get(0)?.senderDetails?.nick_name.toString())
                            }

                            tagsTV.text = data.tag
                            if (data.type == "1"){
                                realTimeTV.text = "Afterwards"
                                dateTV.text = data.start_date.toString()
//                              etDate2.text = data.end_date.toString()
                                timeTV.text = data.start_time.toString()+" - "+data.end_time.toString()
                            }
                            else {
                                dateTV.gone()
                                timeTV.gone()
                                clockIV.gone()
                                calendarIV.gone()
                                realTimeTV.text = "Real Time"
                            }
                        }
                    }
                }
                if (value.apiEndpoint == ACCEPT_REJECT_REQUEST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                    showSuccessAlert(result.message.toString())
                }
            }
            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }

            else -> {

            }
        }
    }

}