package com.live.humanmesh.view.fragment

import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.databinding.FragmentRegisterBinding
import com.live.humanmesh.model.ProfileResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isValidEmail
import com.live.humanmesh.utils.AppUtils.showMaterialDatePicker
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class EditProfileFragment : Fragment(), Observer<Resource<JsonElement>> {
    private lateinit var binding : FragmentRegisterBinding
    private lateinit var appViewModel: AppViewModel
    private var latitude = ""
    private var longitude = ""
    val addressBuilder = StringBuilder()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place: Place? = result.data?.let { Autocomplete.getPlaceFromIntent(it) }
                place?.let { it ->
                    val addresses: List<Address>
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    it.latLng?.let {
                        latitude = it.latitude.toString()
                    }
                    it.latLng?.let { longitude = it.longitude.toString() }
                    addresses = geocoder.getFromLocation(
                        latitude.toDouble(),
                        longitude.toDouble(),
                        1
                    ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    /*if (addresses[0].adminArea != null) {
                        val state: String = addresses[0].adminArea
                    }*/

                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        saveIntoDatabase(Constants.LATITUDE, latitude)
                        saveIntoDatabase(Constants.LONGITUDE, longitude)
                        // Extract the city name using the 'locality' property.
                        /*cityName = address.toString()
//                        cityName = address.locality
                        if (cityName != null) {
                            Log.d("City", "City name: $cityName")
                            binding?.etLoc?.setText(cityName)
                        } else {
                            Log.d("City", "City name not available")
                        }*/
                        addressBuilder.clear()
                        address.subLocality?.let { addressBuilder.append("$it, ") }
                        address.thoroughfare?.let { addressBuilder.append("$it, ") }
                        address.locality?.let { addressBuilder.append("$it, ") }
                        address.countryName?.let { addressBuilder.append("$it, ") }
                        address.postalCode?.let { addressBuilder.append(it) }
                        binding?.etLoc?.setText(addressBuilder.toString())
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.map_key))
        }
        setData()
        clickListeners()
    }
    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        launcher.launch(intent)
    }

    private fun setData() {
        binding.apply {
            binding.toolBar.tvTitle.text = getString(R.string.edit_profile)
            binding.tvPassword.gone()
            binding.etPass.gone()
            binding.tvConfPassword.gone()
            binding.etConfPass.gone()
            binding.ivTick.gone()
            binding.tvTermsText.gone()
            binding.password.gone()
            binding.confirmPwd.gone()
            binding.tvt.gone()
            binding.tvLogin.gone()
            binding.tvGender.gone()
            binding.genderRL.gone()
            binding.tvDob.gone()
            binding.etDob.gone()
            binding.continueBT.text = getString(R.string.update)
            if (arguments != null) {
                etName.setText(requireArguments().getString("name"))
                etNickName.setText(requireArguments().getString("nick_name"))
                etEmail.setText(requireArguments().getString("email"))
                etEmail.isEnabled = false
                etPhone.isEnabled = false
                countryPicker.gone()
                codeTV.visible()
                etPhone.setText(requireArguments().getString("phone"))
                addressBuilder.append(requireArguments().getString("location"))
                etLoc.text = requireArguments().getString("location")
                etDob.text = requireArguments().getString("dob")
                codeTV.text = requireArguments().getString("country_code")?:"0"
                latitude = requireArguments().getString("latitude").toString()
                longitude = requireArguments().getString("longitude").toString()
            }
            binding.countryPicker.setOnClickListener {
                // override to block click
            }
        }
    }

    private fun validateUser(): Boolean {
        binding?.apply {
            if (etName.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_name))
                return false
            } else if (etNickName.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_nick_name))
                return false
            }else if (etLoc.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_location))
                return false
            }
            else if (etDob.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_dob))
                return false
            }
            else
                return true
        }
        return false
    }

    private fun hitEditProfile(){
        val hashMap = HashMap<String, String>()
        binding.apply {
            hashMap["name"] = etName.text.toString().trim()
            hashMap["nick_name"] = etNickName.text.toString().trim()
            hashMap["location"] = addressBuilder.toString()
            hashMap["dob"] = binding.etDob.text.toString()
            hashMap["latitude"] = latitude
            hashMap["longitude"] = longitude
        }
        appViewModel.postApi(Constants.EDIT_PROFILE, hashMap,  requireContext(), true)
            .observe(viewLifecycleOwner, this)

    }
    private fun clickListeners() {
        binding.apply {
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            etLoc.setOnClickListener {
                openPlacePicker()
            }
            continueBT.setOnClickListener {
                if (validateUser()) {
                    hitEditProfile()
                }
            }
            etDob.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager, binding?.etDob)
            }
        }
    }


    override fun onChanged(value: Resource<JsonElement>) {
        when(value.status){
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.EDIT_PROFILE) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), ProfileResponse::class.java)
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