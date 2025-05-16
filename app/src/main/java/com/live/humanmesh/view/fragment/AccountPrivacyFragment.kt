package com.live.humanmesh.view.fragment

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
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.databinding.FragmentAccountPrivacyBinding
import com.live.humanmesh.model.ResendOtpRespone
import com.live.humanmesh.model.VerifyOtpResponse
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.ACCOUNT_PRIVACY
import com.live.humanmesh.utils.Constants.RESEND_OTP
import com.live.humanmesh.utils.Constants.VERIFY_OTP
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AccountPrivacyFragment : Fragment() , Observer<Resource<JsonElement>>{
    private lateinit var binding: FragmentAccountPrivacyBinding
    private lateinit var appViewModel: AppViewModel
    var publicAccount =  0
    var nickNameShow = 0
    var photoVisible = 0
    var eventDetailsShow = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { binding = FragmentAccountPrivacyBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        binding.toolBar.tvTitle.text = getString(R.string.account_privacy)
        binding.toolBar.ivBack.setOnClickListener { findNavController().popBackStack() }
        setData()
        onClick()
    }

    private fun setData() {
      /*  saveIntoDatabase(Constants.USER_PRIVATE_ACCOUNT, publicAccount.toString())
        saveIntoDatabase(Constants.USER_SHOW_NICK_NAME, nickNameShow.toString())
        saveIntoDatabase(Constants.USER_PHOTO_VISIBLE, photoVisible.toString())
        saveIntoDatabase(Constants.USER_SHOW_EVENT_DETAILS, eventDetailsShow.toString())*/

        if (getFromDatabase(Constants.USER_PRIVATE_ACCOUNT,"") == "1")
            binding.switchCompat1.setChecked(true) else binding.switchCompat1.setChecked(false)
        if (getFromDatabase(Constants.USER_SHOW_NICK_NAME,"") == "1")
            binding.switchCompat.setChecked(true) else binding.switchCompat.setChecked(false)
        if (getFromDatabase(Constants.USER_PHOTO_VISIBLE,"") == "1")
            binding.photoSwitchCompat.setChecked(true) else binding.photoSwitchCompat.setChecked(false)
        if (getFromDatabase(Constants.USER_SHOW_EVENT_DETAILS,"") == "1")
            binding.eventSwitchCompat.setChecked(true) else binding.eventSwitchCompat.setChecked(false)
    }

    private fun hitUpdateApi(){
        /*type 1 == account_public
        type 2 == nick_name_show
        type 3 == photo_visiblity
        type 4 == event_details_show
        type 5 == interest_show*/
        publicAccount = if (binding.switchCompat1.isChecked) 1 else 0
        nickNameShow = if (binding.switchCompat.isChecked) 1 else 0
        photoVisible = if (binding.photoSwitchCompat.isChecked) 1 else 0
        eventDetailsShow = if (binding.eventSwitchCompat.isChecked) 1 else 0
        val hashMap = HashMap<String, String>()
        hashMap["1"] = publicAccount.toString()
        hashMap["2"] = nickNameShow.toString()
        hashMap["3"] = photoVisible.toString()
        hashMap["4"] = eventDetailsShow.toString()
        appViewModel.postApi(ACCOUNT_PRIVACY, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)

    }
    private fun onClick() {
        binding.ivDown.setOnClickListener {
            binding.ivDown.visibility = View.GONE
            binding.ivUp.visibility = View.VISIBLE
            binding.llPhoto.visibility = View.VISIBLE
            binding.saveBT.text = getString(R.string.update)
        }
        binding.ivUp.setOnClickListener {
            binding.ivUp.visibility = View.GONE
            binding.ivDown.visibility = View.VISIBLE
            binding.llPhoto.visibility = View.GONE
            binding.saveBT.text = getString(R.string.save)
        }
        binding.saveBT.setOnClickListener {
            hitUpdateApi()
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == ACCOUNT_PRIVACY) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), VerifyOtpResponse::class.java)
                    if (result.code == 200) {
                        saveIntoDatabase(Constants.USER_PRIVATE_ACCOUNT, publicAccount.toString())
                        saveIntoDatabase(Constants.USER_SHOW_NICK_NAME, nickNameShow.toString())
                        saveIntoDatabase(Constants.USER_PHOTO_VISIBLE, photoVisible.toString())
                        saveIntoDatabase(Constants.USER_SHOW_EVENT_DETAILS, eventDetailsShow.toString())
                        showSuccessAlert(result.message)
                        findNavController().popBackStack()
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