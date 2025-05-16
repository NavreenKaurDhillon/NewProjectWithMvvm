package com.live.humanmesh.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.FragmentMyEventsBinding
import com.live.humanmesh.databinding.MenuLayoutBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.model.EventBody
import com.live.humanmesh.model.EventsListResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.ACCEPT_REJECT_REQUEST
import com.live.humanmesh.utils.Constants.DELETE_EVENT
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.adapter.RequestsAdapter
import com.live.humanmesh.view.adapter.TextsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyEventsFragment : Fragment(), Observer<Resource<JsonElement>> {
    private lateinit var binding: FragmentMyEventsBinding
    private lateinit var appViewModel: AppViewModel
    private var eventType = 0
    private  var selectedEventId =""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyEventsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        binding.toolBar.tvTitle.text = getString(R.string.my_events)
        binding.toolBar.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val textsAdapter = TextsAdapter()
        binding.textRV.adapter = textsAdapter
        hitGetEvents()
        clickListeners()
    }

    private fun hitGetEvents() {
        val hashMap = HashMap<String, String>()
        hashMap["type"] = eventType.toString()
        appViewModel.postApi(Constants.MY_EVENTS, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)

    }

    private fun setRequestsAdapter(body: List<EventBody>) {
        val requestsAdapter = RequestsAdapter(body, "mine")
        binding.requestsRV.adapter = requestsAdapter
        requestsAdapter.clickListener = { type, eventId ->
            selectedEventId = eventId
            when (type) {
                "base" -> {
                    val bundle = Bundle()
                    bundle.putString("from", "mine")
                    bundle.putString("eventId", eventId)
                    findNavController().navigate(R.id.my_event_fragment_to_edit_event, bundle)
                }

                "accept" -> {
                    hitAcceptReject(1, eventId)
                }

                "reject" -> {
                    hitAcceptReject(2, eventId)
                }

                "delete" -> {
                    showDeletePostDialog()
                }

                "edit" -> {
                    val bundle = Bundle()
                    bundle.putString("eventId", eventId)
                    findNavController().navigate(R.id.my_event_fragment_to_edit_event, bundle)
                }
            }
        }
    }

    private fun hitAcceptReject(status: Int, eventId: String) {
        val hashMap = HashMap<String, String>()
        hashMap["status"] = status.toString()
        hashMap["eventId"] = eventId
        appViewModel.postApi(ACCEPT_REJECT_REQUEST, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun clickListeners() {
        binding.apply {
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            layoutCL.setOnClickListener {
                findNavController().navigate(R.id.detailsFragment)
            }
            tvAfter.setOnClickListener {
                eventType = 1
                hitGetEvents()
                tvReal.setBackgroundResource(R.drawable.bg_edit_text_less_border)
                tvReal.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black_30))
                tvAfter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                tvAfter.setBackgroundResource(R.drawable.bg_edit_text_less_border_blue)
            }
            tvReal.setOnClickListener {
                eventType = 0
                hitGetEvents()
                tvAfter.setBackgroundResource(R.drawable.bg_edit_text_less_border)
                tvAfter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black_30))
                tvReal.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                tvReal.setBackgroundResource(R.drawable.bg_edit_text_less_border_blue)
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
        binding.tvReport.text = getString(R.string.edit)
        binding.tvClearChat.text = getString(R.string.delete)
        binding.tvBlock.gone()
        binding.v2.gone()
        binding.tvReport.setOnClickListener {
            popupWindow.dismiss()
            findNavController().navigate(R.id.my_event_fragment_to_edit_event)
        }
        binding.tvClearChat.setOnClickListener {
            showDeletePostDialog()
            popupWindow.dismiss()
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        binding.root.setOnClickListener {
            popupWindow.dismiss()

        }
        popupWindow.showAsDropDown(view)
    }

    private fun showDeletePostDialog() {
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
            tvHeading.text = getString(R.string.delete)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_delete_this_post)
            btnYes.setOnClickListener {
                dialog.dismiss()
                hitDeleteEvent()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun hitDeleteEvent() {
        appViewModel.deleteApi(DELETE_EVENT +"$selectedEventId", requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.MY_EVENTS) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        EventsListResponse::class.java
                    )
                    if (result.body.isEmpty())
                        binding.noDataTV.visible()
                    else
                        binding.noDataTV.gone()
                    setRequestsAdapter(result.body)
                }
                if (value.apiEndpoint == ACCEPT_REJECT_REQUEST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                    showSuccessAlert(result.message.toString())
                    hitGetEvents()
                }
                if (value.apiEndpoint == DELETE_EVENT +"$selectedEventId") {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                    showSuccessAlert(result.message.toString())
                    hitGetEvents()
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
