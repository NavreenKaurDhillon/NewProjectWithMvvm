package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.FragmentCreateRequestBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.utils.AppUtils.convertToAmPm
import com.live.humanmesh.utils.AppUtils.createRequestBody
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isValidDateRange
import com.live.humanmesh.utils.AppUtils.isValidTimeRange
import com.live.humanmesh.utils.AppUtils.prepareMultiPart
import com.live.humanmesh.utils.AppUtils.showCurrentDatePicker
import com.live.humanmesh.utils.AppUtils.showMaterialDatePicker
import com.live.humanmesh.utils.AppUtils.showTimePickerDialog
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.imageupload.ImagePickerUtilityFragment
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import java.io.File
import java.util.Locale

@AndroidEntryPoint
class CreateRequestFragment : ImagePickerUtilityFragment(), Observer<Resource<JsonElement>> {

    lateinit var binding: FragmentCreateRequestBinding
    private var eventType = 0
    private var latitude = ""
    private var longitude = ""
    val addressBuilder = StringBuilder()
    private lateinit var appViewModel: AppViewModel
    private var imageUser = ""
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
                        saveIntoDatabase(Constants.LATITUDE, latitude)
                        saveIntoDatabase(Constants.LONGITUDE, longitude)
                        val address = addresses[0]
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCreateRequestBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.map_key))
        }
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        initClickListener()
    }

    private fun createRequestDialog() {
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
            ivTop.setImageResource(R.drawable.ic_tick_blue)
            tvHeading.text = getString(R.string.create_request)
            tvSubHeading.text = getString(R.string.are_you_sure_you_want_to_create_request)
            btnYes.setOnClickListener {
                dialog.dismiss()
                hitCreateEvent()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun hitCreateEvent() {
        val hashMap = HashMap<String, RequestBody>()
        binding.apply {
            hashMap["type"] = createRequestBody(eventType.toString())
            hashMap["latitude"] = createRequestBody(latitude)
            hashMap["location"] = createRequestBody(etLoc.text.toString().trim())
            hashMap["longitude"] = createRequestBody(longitude)
            hashMap["title"] = createRequestBody(etTitle.text.toString().trim())
            hashMap["description"] = createRequestBody(etDesc.text.toString().trim())
//          hashMap["date_time"] = ""
            hashMap["tag"] = createRequestBody(etTags.text.toString().trim())
            //afterwards
            if (eventType == 1) {
                hashMap["start_time"] = createRequestBody(etTime1.text.toString().trim())
                hashMap["end_time"] = createRequestBody(etTime2.text.toString().trim())
                hashMap["start_date"] = createRequestBody(etDate1.text.toString().trim())
                hashMap["end_date"] = createRequestBody(etDate2.text.toString().trim())
            }
        }
        if (imageUser != "") {
            val map: java.util.HashMap<String, RequestBody> = java.util.HashMap()
            map["type"] = createRequestBody("image")
            map["folder"] = createRequestBody("users")
            val image = prepareMultiPart("image", File(imageUser))
            appViewModel.postWithMultipart(
                Constants.ADD_EVENT,
                hashMap,
                requireContext(),
                true,
                image
            ).observe(viewLifecycleOwner, this)
        }
    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        launcher.launch(intent)
    }

    private fun initClickListener() {
        binding.apply {
            toolBar.tvTitle.text = getString(R.string.create_request)
            toolBar.ivBack.visible()
            continueBT.setOnClickListener {
                findNavController().popBackStack()
            }
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivImage.setOnClickListener {
                getImage()
            }
            tvAfter.setOnClickListener {
                eventType = 1
                clDate.visible()
                tvDate.visible()
                clTime.visible()
                tvTime.visible()
                tvReal.setBackgroundResource(R.drawable.bg_edit_text_less_border)
                tvReal.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black_30))
                tvAfter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                tvAfter.setBackgroundResource(R.drawable.bg_edit_text_less_border_blue)
            }
            tvReal.setOnClickListener {
                eventType = 0
                clDate.gone()
                tvDate.gone()
                clTime.gone()
                tvTime.gone()
                tvAfter.setBackgroundResource(R.drawable.bg_edit_text_less_border)
                tvAfter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black_30))
                tvReal.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                tvReal.setBackgroundResource(R.drawable.bg_edit_text_less_border_blue)
            }
            etLoc.setOnClickListener {
                openPlacePicker()
            }
            continueBT.setOnClickListener {
                if (validatePost())
                    createRequestDialog()
            }
            etDate1.setOnClickListener {
                showCurrentDatePicker(requireActivity().supportFragmentManager, etDate1)
            }
            etDate2.setOnClickListener {
                showCurrentDatePicker(requireActivity().supportFragmentManager, etDate2)
            }
            etTime1.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime1.text = it
                }
            }
            etTime2.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime2.text = it
                }
            }
        }
    }

    private fun validatePost(): Boolean {
        binding.apply {
            if (imageUser.isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_select_image))
                return false
            } else if (etTitle.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_title))
                return false
            } else if (eventType == 1 && etDate1.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_select_start_date))
                return false
            } else if (eventType == 1 && etDate2.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_select_end_date))
                return false
            } else if (eventType == 1 && !isValidDateRange(
                    etDate1.text.toString().trim(),
                    etDate2.text.toString().trim()
                )
            ) {
                showErrorAlert(resources.getString(R.string.please_select_valid_date_range))
                return false
            } else if (eventType == 1 && etTime1.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_select_start_time))
                return false
            } else if (eventType == 1 && etTime2.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_select_end_time))
                return false
            } else if (eventType == 1 && !isValidTimeRange(
                    etTime1.text.toString().trim(),
                    etTime2.text.toString().trim()
                )
            ) {
                showErrorAlert(resources.getString(R.string.please_select_valid_end_time))
                return false
            } else if (etLoc.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_location))
                return false
            } else if (etDesc.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_descripion))
                return false
            } else if (etTags.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_tags))
                return false
            } else
                return true
        }
        return false
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.ADD_EVENT) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                    if (result.code == 200)
                        showSuccessAlert("Request created successfully.")
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


    /*  override fun selectedImage(imagePath: String?, code: Int) {
          val file = File(imagePath)
          if (!file.exists()) {
              Log.e("ImageLoading", "File not found at path: $imagePath")
              // Handle the case where the file doesn't exist (e.g., show a placeholder)
              return
          }
          imageUser = imagePath.toString()
          Glide.with(requireContext()).load(imagePath).into(binding.ivImage)
      }*/

    override fun selectedImage(imagePath: String?, code: Int, type: String) {
        imageUser = imagePath.toString()
        Glide.with(requireContext()).load(imagePath).into(binding.ivImage)
    }


}