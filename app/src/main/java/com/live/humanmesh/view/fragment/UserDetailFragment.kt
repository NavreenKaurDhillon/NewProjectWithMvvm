package com.live.humanmesh.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveTutorialIntoDatabase
import com.live.humanmesh.databinding.FragmentOtherUserProfileBinding
import com.live.humanmesh.model.LoginResponse
import com.live.humanmesh.model.ProfileResponse
import com.live.humanmesh.utils.AppUtils.maskString
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.LOG_IN
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.view.activity.HomeActivity
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment(), Observer<Resource<JsonElement>> {

    private lateinit var binding: FragmentOtherUserProfileBinding
    private lateinit var appViewModel: AppViewModel
    private var userId = ""
    private var profileVisitPermission = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtherUserProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        if (arguments != null)
            if (requireArguments().getString("userId") != null)
                userId = requireArguments().getString("userId").toString()
        if (requireArguments().getString("profileVisitPermission") != null)
            profileVisitPermission = requireArguments().getString("profileVisitPermission").toString()
        hitDetailApi()
        clicks()
    }

    private fun clicks() {
        binding.apply {
            toolBar.tvTitle.text = "User Profile"
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun hitDetailApi() {
        val hashMap = HashMap<String, String>()
        hashMap["id"] = userId
        appViewModel.postApi(Constants.OTHER_USER_PROFILE, hashMap, requireContext(), false)
            .observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.OTHER_USER_PROFILE) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        ProfileResponse::class.java
                    )
                    if (result.code == 200) {
                        if (result.body.gender == "0")
                            binding.imageView.setImageResource(R.drawable.gray_male)
                        else
                            binding.imageView.setImageResource(R.drawable.gray_female)
                        if (result.body.account_public == "1" || profileVisitPermission == "1" )
                            binding.nameTV.text = result.body.name
                        else
                            binding.nameTV.text = maskString(result.body.name)
//                        if (result.body.nick_name_show == "1")
                        binding.nickNameTV.text = result.body.nick_name
//                        else
//                            binding.nickNameTV.text = maskString(result.body.nick_name)

                        binding.locationTV.text = result.body.location
                        binding.timeTV.text = result.body.phone_number
                        binding.dateTV.text = result.body.dob
                    } else {
                        showErrorAlert(result.message.toString())
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