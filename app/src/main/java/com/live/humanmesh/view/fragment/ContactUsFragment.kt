package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.FragmentContactUsBinding
import com.live.humanmesh.model.ContactUsResponse
import com.live.humanmesh.model.FaqResponse
import com.live.humanmesh.utils.AppUtils.isValidEmail
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactUsFragment : Fragment(), Observer<Resource<JsonElement>> {
    lateinit var binding :FragmentContactUsBinding
    private lateinit var appViewModel: AppViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding =FragmentContactUsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        setData()
        initClickListener()
    }

    private fun setData() {
        binding.apply {
            etEmail.setText(getFromDatabase(Constants.USER_MAIL,""))
            etPhone.setText(getFromDatabase(Constants.USER_PHONE,""))
            countryPicker.setCountryForPhoneCode(getFromDatabase(Constants.USER_COUNTRY_CODE,"").toInt())
        }
    }

    private fun hitContactUsApi(){
       /* name:harmandeep ff
        country_code:+91
        email:harmandeep11@gmail.com
        message:fggggggggggggggggggggg
        phone_number:6666666666*/

        val hashMap = HashMap<String, String>()
        hashMap["name"] = binding.etName.text.toString().trim()
        hashMap["country_code"] = binding.countryPicker.selectedCountryCodeWithPlus.toString().trim()
        hashMap["email"] = binding.etEmail.text.toString().trim()
        hashMap["message"] = binding.etMessage.text.toString().trim()
        hashMap["phone_number"] = binding.etPhone.text.toString().trim()
        appViewModel.postApi(Constants.CONTACT_US, hashMap,requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }
    private fun validateUser(): Boolean {
        binding?.apply {
            if (etName.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_name))
                return false
            }  else if (etEmail.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_email))
                return false
            } else if (!isValidEmail(etEmail.text.toString().trim())) {
                showErrorAlert(resources.getString(R.string.please_enter_valid_email))
                return false
            } else if (etPhone.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_phone))
                return false
            } else if (etPhone.text.toString().length < 7 || etPhone.text.toString().length > 12) {
                showErrorAlert(resources.getString(R.string.phone_8_characters))
                return false
            }
           else if (etMessage.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_your_message))
                return false
            }
            else
                return true
        }
        return false
    }


    @SuppressLint("SetTextI18n")
    private fun initClickListener() {
        binding!!.apply {
            toolBar.tvTitle.text = getString(R.string.contact_us)
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            submitBT.setOnClickListener {
                if (validateUser()) {
                    hitContactUsApi()
                }
            }

        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.CONTACT_US) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        ContactUsResponse::class.java)
                   showSuccessAlert(result.message)
                    findNavController().popBackStack()
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