package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.live.humanmesh.databinding.FragmentEditEventBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.model.EventDetailResponse
import com.live.humanmesh.utils.AppUtils.createRequestBody
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isValidDateRange
import com.live.humanmesh.utils.AppUtils.isValidTimeRange
import com.live.humanmesh.utils.AppUtils.prepareMultiPart
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
class EditEventFragment: ImagePickerUtilityFragment(), Observer<Resource<JsonElement>>
{

    lateinit var binding:FragmentEditEventBinding
    private var eventType = 0
    private var eventId = ""
    private var latitude = ""
    private var longitude = ""
    val addressBuilder = StringBuilder()
    private lateinit var appViewModel: AppViewModel
    private var imageUser = ""
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditEventBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.map_key))
        }
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        if (arguments!=null){
            eventId = requireArguments().getString("eventId","")
        }
        initClickListener()
        getEventDetails()
        clicks()
    }

    private fun openPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
        launcher.launch(intent)
    }

    @SuppressLint("NewApi")
    private fun initClickListener() {
        binding.apply {
            toolBar.tvTitle.text = getString(R.string.create_request)
            toolBar.ivBack.visible()
            etLoc.isEnabled = false
            etDate1.isEnabled = false
            etDate2.isEnabled = false
            etTime1.isEnabled = false
            etTime2.isEnabled = false
            continueBT.setOnClickListener {
                findNavController().popBackStack()
            }
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivImage.setOnClickListener {
                Log.d("wkjbwe", "initClickListener: rlCamera")
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
                    hitEditEvent()
            }
    /*        etDate1.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager, etDate1)
            }
            etDate2.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager, etDate2)
            }
            etTime1.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime1.text = it }
            }
            etTime2.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime2.text = it }
            }*/
        }
    }


    private fun clicks() {
        binding.apply {

            binding.toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            binding.toolBar.tvTitle.text = getString(R.string.edit)

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
            continueBT.setOnClickListener {
                if (validatePost())
                    hitEditEvent()
            }
            etLoc.setOnClickListener {
                openPlacePicker()
            }
            etDate1.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager, etDate1)
            }
            etDate2.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager, etDate2)
            }
            etTime1.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime1.text = it }
            }
            etTime2.setOnClickListener {
                showTimePickerDialog(requireContext()) {
                    etTime2.text = it }
            }
        }

    }

    private fun validatePost() : Boolean{
        binding.apply {

            if (etTitle.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_enter_title))
                return false
            }
            else  if (eventType==1 && etDate1.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_select_start_date))
                return false
            }
            else if (eventType==1 && etDate2.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_select_end_date))
                return false
            }
            else if (eventType==1 && !isValidDateRange(etDate1.text.toString().trim(), etDate2.text.toString().trim())){
                showErrorAlert(resources.getString(R.string.please_select_valid_date_range))
                return false
            }
            else if (eventType==1 && etTime1.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_select_start_time))
                return false
            }
            else if (eventType==1 && etTime2.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_select_end_time))
                return false
            }
            else if (eventType==1 && !isValidTimeRange(etTime1.text.toString().trim(), etTime2.text.toString().trim()))
            {
                showErrorAlert(resources.getString(R.string.please_select_valid_end_time))
                return false
            }
            else if (etLoc.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_enter_location))
                return false
            }
            else if (etDesc.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_enter_descripion))
                return false
            }
            else if (etTags.text.toString().trim().isEmpty()){
                showErrorAlert(resources.getString(R.string.please_enter_tags))
                return false
            }
            else
                return true
        }
        return false
    }

    private fun hitEditEvent() {
        if (imageUser!="") {
            val hashMap = HashMap<String, RequestBody>()
            val map: java.util.HashMap<String, RequestBody> = java.util.HashMap()
            map["type"] = createRequestBody("image")
            map["folder"] = createRequestBody("users")
            val image = prepareMultiPart("image", File(imageUser))
            hashMap["type"] = createRequestBody(eventType.toString())
            hashMap["latitude"] =  createRequestBody(latitude)
            hashMap["location"] =  createRequestBody(binding.etLoc.text.toString().trim())
            hashMap["longitude"] =  createRequestBody(longitude)
            hashMap["title"] =  createRequestBody(binding.etTitle.text.toString().trim())
            hashMap["description"] =  createRequestBody(binding.etDesc.text.toString().trim())
//          hashMap["date_time"] = ""
            hashMap["event_tag"] =  createRequestBody(binding.etTags.text.toString().trim())
            //afterwards
            if (eventType == 1) {
                hashMap["start_time"] =  createRequestBody(binding.etTime1.text.toString().trim())
                hashMap["end_time"] = createRequestBody(binding.etTime2.text.toString().trim())
                hashMap["start_date"] = createRequestBody(binding.etDate1.text.toString().trim())
                hashMap["end_date"] = createRequestBody(binding.etDate2.text.toString().trim())

            }
            appViewModel.postWithMultipart(Constants.EDIT_EVENT+"$eventId", hashMap, requireContext(), true, image).observe(viewLifecycleOwner, this)
        }
        else{
            val hashMap = HashMap<String, String>()
            hashMap["type"] = eventType.toString()
            hashMap["latitude"] =  latitude
            hashMap["location"] =  binding.etLoc.text.toString().trim()
            hashMap["longitude"] =  longitude
            hashMap["title"] =  binding.etTitle.text.toString().trim()
            hashMap["description"] =  binding.etDesc.text.toString().trim()
            hashMap["event_tag"] =  binding.etTags.text.toString().trim()
            if (eventType == 1) {
                hashMap["start_time"] =  binding.etTime1.text.toString().trim()
                hashMap["end_time"] = binding.etTime2.text.toString().trim()
                hashMap["start_date"] = binding.etDate1.text.toString().trim()
                hashMap["end_date"] = binding.etDate2.text.toString().trim()
            }
            appViewModel.postApi(Constants.EDIT_EVENT+"$eventId", hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
        }
    }

    private fun getEventDetails() {
        binding.tvType.gone()
        binding.tvReal.gone()
        binding.tvAfter.gone()
        val  hashMap = HashMap<String, String>()
        hashMap["eventId"] = eventId
        appViewModel.postApi(Constants.REQUEST_DETAILS, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
    }


    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.REQUEST_DETAILS) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        EventDetailResponse::class.java)
                    val data = result.body.Request
                    binding.apply {
                        Glide.with(requireContext()).load(Constants.IMAGES_BASE_URL+result.body.Request.image)
                            .into(ivImage)
                        etTitle.setText(data.title)
                        etDesc.setText(data.description)
                        etLoc.text = data.location
                        etTags.setText(data.tag)
                        if (data.type == "1"){
                            etDate1.text = data.start_date.toString()
                            etDate2.text = data.end_date.toString()
                            etTime1.text = data.start_time.toString()
                            etTime2.text = data.end_time.toString()
                            tvAfter.isEnabled= false
                            binding.apply {
                                clDate.visible()
                                tvDate.visible()
                                clTime.visible()
                                tvTime.visible()
                            }
                        }
                        else
                            tvReal.isEnabled = false
                    }
                }
                if (value.apiEndpoint == Constants.EDIT_EVENT+"$eventId") {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        CommonResponse::class.java)
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



    override fun selectedImage(imagePath: String?, code: Int, type: String) {
        imageUser = imagePath.toString()
        Glide.with(requireContext()).load(imagePath).into(binding.ivImage)
    }

}