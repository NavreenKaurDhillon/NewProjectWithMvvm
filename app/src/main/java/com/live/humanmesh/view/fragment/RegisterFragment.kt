package com.live.humanmesh.view.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveTutorialIntoDatabase
import com.live.humanmesh.databinding.DialogAccountCreatedSuccessfullyBinding
import com.live.humanmesh.databinding.FragmentRegisterBinding
import com.live.humanmesh.model.LocationData
import com.live.humanmesh.model.SignupResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isValidEmail
import com.live.humanmesh.utils.AppUtils.showMaterialDatePicker
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.SIGN_UP
import com.live.humanmesh.utils.LocationUpdateUtility
import com.live.humanmesh.utils.setupPasswordToggle
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.activity.HomeActivity
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class RegisterFragment : Observer<Resource<JsonElement>>,
    LocationUpdateUtility<FragmentRegisterBinding>() {

    private lateinit var appViewModel: AppViewModel
    private lateinit var registerBinding: FragmentRegisterBinding
    private var isCheckedPassword = false
    private var isCheckedConfirmPassword = false
    private var isCheckedTerms = false
    private var latitude = ""
    private var longitude = ""
    private var gender = "0"
    val addressBuilder = StringBuilder()
    private var socialId =""
    private var socialType =""


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place: Place? = result.data?.let { Autocomplete.getPlaceFromIntent(it) }
                place?.let { it ->
                    val addresses: List<Address>
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    it.latLng?.let { latitude = it.latitude.toString() }
                    it.latLng?.let { longitude = it.longitude.toString() }
                    addresses = geocoder.getFromLocation(
                        latitude.toDouble(), longitude.toDouble(), 1
                    ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        addressBuilder.clear()
                        address.subLocality?.let { addressBuilder.append("$it, ") }
                        address.thoroughfare?.let { addressBuilder.append("$it, ") }
                        address.locality?.let { addressBuilder.append("$it, ") }
                        address.countryName?.let { addressBuilder.append("$it, ") }
                        address.postalCode?.let { addressBuilder.append(it) }
                        registerBinding.etLoc.text = addressBuilder.toString()
                    }
                }
            }
        }

    override fun getLayoutRes(): Int = R.layout.fragment_register


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        registerBinding = FragmentRegisterBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return registerBinding.root
    }

    override fun updatedLatLng(loc: LocationData) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(
            loc.latitude, loc.longitude.toDouble(), 1
        ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        latitude = loc.latitude.toString()
        longitude = loc.longitude.toString()
        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            addressBuilder.clear()
            address.subLocality?.let { addressBuilder.append("$it, ") }
            address.thoroughfare?.let { addressBuilder.append("$it, ") }
            address.locality?.let { addressBuilder.append("$it, ") }
            address.countryName?.let { addressBuilder.append("$it, ") }
            address.postalCode?.let { addressBuilder.append(it) }
            registerBinding.etLoc.text = addressBuilder.toString()
        }
    }

    override fun showProgress() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.map_key))
        }
        if (getFromDatabase(Constants.DEVICE_TOKEN, "").isEmpty())
            getFireBaseToken()

        checkData()
        initClickListener()
        showHidePassword(isCheckedPassword)
        showHideConfirmPassword(isCheckedConfirmPassword)
        setGenderAdapter()

        try {
            CoroutineScope(Dispatchers.IO).launch {
                while (isActive) {
                    if (isAdded)
                        getLiveLocation(requireActivity())
                    delay(90000L) // Delay for 10 minutes (in milliseconds)
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun checkData() {
        registerBinding.apply {
            if (arguments != null) {
                if (requireArguments().getString("email") != null) {
                    etEmail.setText(requireArguments().getString("email"))
                    if (requireArguments().getString("name") != null)
                        etName.setText(requireArguments().getString("name"))
                    if (requireArguments().getString("nick_name") != null)
                        etNickName.setText(requireArguments().getString("nick_name"))
                    if (requireArguments().getString("social_id") != null)
                        socialId = requireArguments().getString("social_id").toString()
                    if (requireArguments().getString("social_type") != null)
                        socialType = requireArguments().getString("social_type").toString()
                    tvPassword.gone()
                    password.gone()
                    etPass.gone()
                    tvConfPassword.gone()
                    confirmPwd.gone()
                    etConfPass.gone()
                }
            }
        }
    }

    private fun getFireBaseToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result
                saveIntoDatabase(Constants.DEVICE_TOKEN, token)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setGenderAdapter() {
        val genders = ArrayList<String>()
        genders.add("Male")
        genders.add("Female")
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.textview_item, genders)
        genderAdapter.setDropDownViewResource(R.layout.dropdown_spinner_item)
        registerBinding.genderSpinner.adapter = genderAdapter
        registerBinding.genderSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (parent?.getItemAtPosition(position).toString() == "Male")
                        gender = "0"
                    else
                        gender = "1"
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where no item is selected (if necessary)
                }
            }
    }

    private fun initClickListener() {
        registerBinding.apply {
            toolBar.tvTitle.text = requireActivity().getString(R.string.register)
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivTick.setOnClickListener {
                isCheckedTerms = !isCheckedTerms
                if (isCheckedTerms)
                    ivTick.setImageResource(R.drawable.ic_selected)
                else
                    ivTick.setImageResource(R.drawable.ic_unselected)
            }
            tvLogin.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            continueBT.setOnClickListener {
                if (validateRegister())
                    hitRegisterApi()
            }
            etLoc.setOnClickListener {
                openPlacePicker()
            }
            etDob.setOnClickListener {
                showMaterialDatePicker(requireActivity().supportFragmentManager,
                    registerBinding.etDob)
            }
            etPass.setupPasswordToggle(imageView = password,
                visibleIcon = R.drawable.eye, hiddenIcon = R.drawable.eye_hide)
            etConfPass.setupPasswordToggle(imageView = confirmPwd,
                visibleIcon = R.drawable.eye, hiddenIcon = R.drawable.eye_hide)
        }
    }

    private fun hitRegisterApi() {
        val hashMap = HashMap<String, String>()
        registerBinding.apply {
            hashMap["name"] = etName.text.toString().trim()
            hashMap["nick_name"] = etNickName.text.toString().trim()
            hashMap["email"] = etEmail.text.toString().trim()
            hashMap["device_type"] = "2"
            hashMap["country_code"] = registerBinding.countryPicker.selectedCountryCodeWithPlus.toString()
            hashMap["phone_number"] = etPhone.text.toString().trim()
            hashMap["location"] = addressBuilder.toString()
            hashMap["device_token"] = getFromDatabase(Constants.DEVICE_TOKEN, "")
            hashMap["gender"] = gender
            hashMap["latitude"] = latitude
            hashMap["dob"] = registerBinding.etDob?.text.toString()
            if (socialId!="") {
                hashMap["social_id"] = socialId
                hashMap["social_type"] = socialType
            } else{
                hashMap["password"] = etNickName.text.toString().trim()
                hashMap["confirm_password"] = etNickName.text.toString().trim()
            }
            hashMap["longitude"] = longitude
            hashMap["role"] = "1"
            if (etCode.text.toString().trim().isNotEmpty())
                hashMap["other_referal_code"] = etCode.text.toString().trim()
        }
        for ((key, value) in hashMap)
            Log.d("WEJKGJHWE", "hitRegisterApi: $key $value")
        appViewModel.postApi(SIGN_UP, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
    }


    private fun validateRegister(): Boolean {
        registerBinding.apply {
            if (etName.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_name))
                return false
            } else if (etNickName.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_nick_name))
                return false
            } else if (etEmail.text.toString().trim().isEmpty()) {
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
            } else if (etLoc.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_location))
                return false
            } else if (etDob.text.toString().trim().isEmpty()) {
                showErrorAlert(resources.getString(R.string.please_enter_dob))
                return false
            } else if (socialId=="") {
                if (etPass.text.toString().trim().isEmpty()) {
                    showErrorAlert(resources.getString(R.string.please_enter_password))
                    return false
                } else if (etPass.text.toString().trim().length < 6) {
                    showErrorAlert(resources.getString(R.string.password_6_characters))
                    return false
                } else if (etConfPass.text.toString().trim().isEmpty()) {
                    showErrorAlert(resources.getString(R.string.please_enter_conf_password))
                    return false
                } else if (etPass.text.toString().trim() == etConfPass.text.toString()
                        .trim() == false
                ) {
                    showErrorAlert(resources.getString(R.string.confirm_password_same))
                    return false
                }
            } else if (!isCheckedTerms) {
                showErrorAlert(resources.getString(R.string.please_accept_terms))
                return false
            } else
                return true
        }
        return false
    }

    private fun showHidePassword(isChecked: Boolean) {
        if (isChecked) {
            registerBinding.etPass.transformationMethod = null
            registerBinding.etPass.setSelection(registerBinding.etPass.length())
            Glide.with(this).load(R.drawable.eye).into(registerBinding.password)
        } else {
            registerBinding.etPass.transformationMethod = PasswordTransformationMethod()
            registerBinding.etPass.setSelection(registerBinding.etPass.length())
            Glide.with(this).load(R.drawable.eye_hide).into(registerBinding.password)
        }
    }

    private fun showHideConfirmPassword(isChecked: Boolean) {
        if (isChecked) {
            registerBinding.etConfPass.transformationMethod = null
            registerBinding.etConfPass.setSelection(registerBinding.etConfPass.length())
            Glide.with(this).load(R.drawable.eye).into(registerBinding.password)
        } else {
            registerBinding.etConfPass.transformationMethod = PasswordTransformationMethod()
            registerBinding.etConfPass.setSelection(registerBinding.etConfPass.length())
            Glide.with(this).load(R.drawable.eye_hide).into(registerBinding.password)
        }
    }

    private fun openPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
        launcher.launch(intent)
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == SIGN_UP) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), SignupResponse::class.java)
                    if (result.code == 200) {
                        saveTutorialIntoDatabase(Constants.EMAIL_KEY, registerBinding.etEmail.text.toString())
                        saveTutorialIntoDatabase(Constants.PASSWORD_KEY, registerBinding.etPass.text.toString())
                        saveIntoDatabase(Constants.LATITUDE, result.body.latitude.toString())
                        saveIntoDatabase(Constants.LONGITUDE, result.body.longitude)
                        saveTutorialIntoDatabase(Constants.REMEMBER_KEY, "true")
                        saveIntoDatabase(Constants.AUTH_TOKEN, result.body.token.toString())
                        saveIntoDatabase(Constants.USER_ID, result.body.id.toString())
                        saveIntoDatabase(Constants.NICK_NAME, result.body.nick_name.toString())
                        saveIntoDatabase(Constants.COUNTRY_CODE, result.body.country_code.toString())
                        saveIntoDatabase(Constants.NAME, result.body.name.toString())
                        saveIntoDatabase(Constants.USER_LOCATION, result.body.location.toString())
                        saveIntoDatabase(Constants.USER_MAIL, result.body.email.toString())
                        saveIntoDatabase(Constants.USER_GENDER, result.body.gender.toString())
                        saveIntoDatabase(Constants.USER_PRIVATE_ACCOUNT, result.body.account_public.toString())
                        saveIntoDatabase(Constants.USER_SHOW_NICK_NAME, result.body.nick_name_show.toString())
                        saveIntoDatabase(Constants.USER_PHOTO_VISIBLE, result.body.photo_visiblity.toString())
                        saveIntoDatabase(Constants.USER_SHOW_EVENT_DETAILS, result.body.event_details_show.toString())
                        saveIntoDatabase(Constants.USER_PHONE, result.body.phone_number.toString())
                        saveIntoDatabase(Constants.USER_COUNTRY_CODE, result.body.country_code.toString())
                        if (socialId==""){
                            findNavController().popBackStack(R.id.registerFragment, true)
                            findNavController().navigate(R.id.verificationFragment)
                        }
                        else
                            dialogAccountCreated()
                    } else
                        showSuccessAlert(result.message.toString())
                }
            }
            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }
            else -> {}
        }
    }
    private fun dialogAccountCreated() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingAcc = DialogAccountCreatedSuccessfullyBinding.inflate(layoutInflater)
        dialog.setContentView(bindingAcc.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()
        bindingAcc.okBT.setOnClickListener {
            dialog.dismiss()
            saveIntoDatabase(Constants.IS_LOGIN, true)
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

}