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
import com.live.humanmesh.databinding.FragmentForgotPasswordBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.utils.AppUtils.isValidEmail
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.FORGOT_PASSWORD
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.jvm.java

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(), Observer<Resource<JsonElement>> {
    private var binding: FragmentForgotPasswordBinding? = null
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        initClickListener()
    }

    private fun initClickListener() {
        binding!!.apply {
            toolBar.tvTitle.text = requireActivity().getString(R.string.forgot_password)
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            sendBT.setOnClickListener {
                if (etEmail.text.toString().trim().isEmpty())
                    showErrorAlert(resources.getString(R.string.please_enter_email))
                else if (!isValidEmail(etEmail.text.toString().trim()))
                    showErrorAlert(resources.getString(R.string.please_enter_valid_email))
                else
                    hitForgotApi()
            }
        }
    }

    private fun hitForgotApi() {
        val hashMap = HashMap<String, String>()
        hashMap["email"] = binding?.etEmail?.text.toString().trim()
        appViewModel.postApi(Constants.FORGOT_PASSWORD, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                     if (value.apiEndpoint == FORGOT_PASSWORD) {
                         val result = Gson().fromJson(
                             (value.data as JsonElement).toString(),
                             CommonResponse::class.java
                         )
                         if (result.code == 200) {
                             showErrorAlert(result.message)
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