package com.live.humanmesh.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.databinding.FragmentPrivacyPolicyBinding
import com.live.humanmesh.model.TermsConditionsResponse
import com.live.humanmesh.model.VerifyOtpResponse
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PrivacyPolicyFragment : Fragment(), Observer<Resource<JsonElement>> {
    private lateinit var binding: FragmentPrivacyPolicyBinding
    private lateinit var appViewModel: AppViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        if (arguments != null) {
            if (arguments?.getString("title") != null) {
                val title = arguments?.getString("title")
                binding.toolBar.tvTitle.text = title
                hitApi(title.toString())
            }
        }
        binding.toolBar.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun hitApi(type: String) {
        when (type) {
            "Privacy Policy" -> {
                appViewModel.getApi(Constants.TERMS_CONDITIONS, requireContext(), true)
                    .observe(viewLifecycleOwner, this)
            }
            "Terms & Conditions" -> {
                appViewModel.getApi(Constants.PRIVACY_POLICY, requireContext(), true)
                    .observe(viewLifecycleOwner, this)
            }
            "About Us" -> {
                appViewModel.getApi(Constants.ABOUT_US, requireContext(), true)
                    .observe(viewLifecycleOwner, this)
            }
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.TERMS_CONDITIONS) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), TermsConditionsResponse::class.java)
                    binding.descriptionTV.text =  HtmlCompat.fromHtml(result.body.description, HtmlCompat.FROM_HTML_MODE_LEGACY)

                }
                if (value.apiEndpoint == Constants.PRIVACY_POLICY) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), TermsConditionsResponse::class.java)
                    binding.descriptionTV.text =  HtmlCompat.fromHtml(result.body.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
                if (value.apiEndpoint == Constants.ABOUT_US) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), TermsConditionsResponse::class.java)
                    binding.descriptionTV.text =  HtmlCompat.fromHtml(result.body.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
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