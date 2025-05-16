package com.live.humanmesh.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.getTutorialFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveTutorialIntoDatabase
import com.live.humanmesh.databinding.FragmentLoginBinding
import com.live.humanmesh.model.LoginResponse
import com.live.humanmesh.model.SocialLoginResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isEmailValid
import com.live.humanmesh.utils.AppUtils.isValidEmail
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.CHECK_SOCIAL_LOG_IN
import com.live.humanmesh.utils.Constants.LOG_IN
import com.live.humanmesh.utils.google.GoogleHelper
import com.live.humanmesh.utils.setupPasswordToggle
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.view.activity.HomeActivity
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment(), Observer<Resource<JsonElement>>,
    GoogleHelper.GoogleHelperCallback {

    private lateinit var binding: FragmentLoginBinding
    private var isCheckedPassword = true
    private lateinit var appViewModel: AppViewModel
    private lateinit var googleHelper: GoogleHelper
    private val socialMap = java.util.HashMap<String, Any>()
    private var socialType =""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        googleHelper = GoogleHelper(this, this)
        binding.toolBar.tvTitle.text = getString(R.string.login)
        binding.toolBar.ivBack.gone()
        if (getFromDatabase(Constants.DEVICE_TOKEN, "").isEmpty())
            getFireBaseToken()
        checkRememberMe()
        clickListeners()
        showHidePassword(isCheckedPassword)
    }

    private fun loginUser() {
        if (binding.etEmail.text.toString().trim().isEmpty())
            showErrorAlert(resources.getString(R.string.please_enter_email))
        else if (!isValidEmail(binding.etEmail.text.toString().trim()))
            showErrorAlert(resources.getString(R.string.please_enter_valid_email))
        else if (binding.etPassword.text.toString().trim().isEmpty())
            showErrorAlert(resources.getString(R.string.please_enter_password))
        else {
            hitLoginApi()
        }
    }

    private fun getFireBaseToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result
                Log.d("Ftokennnn", "getFireBaseToken: $token")
                saveIntoDatabase(Constants.DEVICE_TOKEN, token)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hitLoginApi() {
        val hashMap = HashMap<String, String>()
//        if (checkNetworkConnection(this)) {
        hashMap["email"] = binding.etEmail.text.toString().trim()
        hashMap["password"] = binding.etPassword.text.toString().trim()
        hashMap["device_token"] = getFromDatabase(Constants.DEVICE_TOKEN, "")
        hashMap["device_type"] = "2"
        appViewModel.postApi(LOG_IN, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
//        }
//        else {
//            showToast(getString(R.string.no_internet_connection))
//        }
    }


    private fun checkRememberMe() {
        if (getTutorialFromDatabase(Constants.REMEMBER_KEY, "") == "true") {
            with(binding) {
                etEmail.setText(
                    getTutorialFromDatabase(
                        Constants.SAVED_EMAIL,
                        ""
                    ).toString()
                )
                etPassword.setText(
                    getTutorialFromDatabase(
                        Constants.SAVED_PASSWORD,
                        ""
                    ).toString()
                )

            }
        }
    }

    private fun clickListeners() {
        binding?.apply {
            tvRegister.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            tvForgot.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
            }
            loginBT.setOnClickListener {
                loginUser()

                /*startActivity(Intent(requireActivity(), HomeActivity::class.java))
                requireActivity().finish()*/
            }
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            etPassword.setupPasswordToggle(
                imageView = password,
                visibleIcon = R.drawable.eye,
                hiddenIcon = R.drawable.eye_hide
            )
            ivGoogle.setOnClickListener {
                socialType = "1"
                googleHelper.signIn()
            }
        }
    }

    private fun showHidePassword(isChecked: Boolean) {
        if (isChecked) {
            binding.etPassword.transformationMethod = null
            binding.etPassword.setSelection(binding.etPassword.length())
            binding.password.let { Glide.with(this).load(R.drawable.eye).into(it) }
        } else {
            binding.etPassword.transformationMethod = PasswordTransformationMethod()
            binding.etPassword.setSelection(binding.etPassword.length())
            Glide.with(this).load(R.drawable.eye_hide).into(binding.password)
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == CHECK_SOCIAL_LOG_IN) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        SocialLoginResponse::class.java
                    )
                    if (result.code == 200) {
                        if (result.body.is_social == 1) {

                        } else {
                            val bundle = Bundle()
                            bundle.putString("email",socialMap["email"].toString())
                            bundle.putString("name",socialMap["name"].toString())
                            bundle.putString("social_id",socialMap["social_id"].toString())
                            bundle.putString("social_type",socialType)
                            bundle.putString("nick_name",socialMap["nick_name"].toString())
                            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, bundle)
                        }
                    }
                }
                if (value.apiEndpoint == LOG_IN) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        LoginResponse::class.java
                    )
                    if (result.code == 200) {
                        Log.d("welkfhkwe", "onChanged: ${binding.switchCompat.isChecked}")
                        if (binding.switchCompat.isChecked == true) {
                            saveTutorialIntoDatabase(
                                Constants.SAVED_EMAIL,
                                binding.etEmail.text.toString()
                            )
                            saveTutorialIntoDatabase(
                                Constants.SAVED_PASSWORD,
                                binding.etPassword.text.toString()
                            )
                            saveTutorialIntoDatabase(Constants.REMEMBER_KEY, "true")
                        } else {
                            saveTutorialIntoDatabase(
                                Constants.EMAIL_KEY,
                                ""
                            )
                            saveTutorialIntoDatabase(
                                Constants.PASSWORD_KEY,
                                ""
                            )
                            saveTutorialIntoDatabase(
                                Constants.REMEMBER_KEY,
                                "false"
                            )
                        }
                        saveIntoDatabase(Constants.AUTH_TOKEN, result.body.token.toString())
                        saveIntoDatabase(Constants.USER_ID, result.body.id.toString())
                        saveIntoDatabase(Constants.USER_LOCATION, result.body.location.toString())
                        saveIntoDatabase(Constants.NAME, result.body.name.toString())
                        saveIntoDatabase(Constants.NICK_NAME, result.body.nick_name.toString())
                        saveIntoDatabase(Constants.USER_MAIL, result.body.email.toString())
                        saveIntoDatabase(Constants.USER_PHONE, result.body.phone_number.toString())
                        saveIntoDatabase(
                            Constants.USER_COUNTRY_CODE,
                            result.body.country_code.toString()
                        )
                        saveIntoDatabase(Constants.USER_GENDER, result.body.gender.toString())
                        saveIntoDatabase(
                            Constants.USER_PRIVATE_ACCOUNT,
                            result.body.account_public.toString()
                        )
                        saveIntoDatabase(
                            Constants.USER_SHOW_NICK_NAME,
                            result.body.nick_name_show.toString()
                        )
                        saveIntoDatabase(
                            Constants.USER_PHOTO_VISIBLE,
                            result.body.photo_visiblity.toString()
                        )
                        saveIntoDatabase(
                            Constants.USER_SHOW_EVENT_DETAILS,
                            result.body.event_details_show.toString()
                        )
                        if (result.body.is_verify == "1") {
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    HomeActivity::class.java
                                )
                            )
                        } else {
                            val bundle = Bundle()
                            bundle.putString("countryCode", result.body.country_code)
                            bundle.putString("phone", result.body.phone_number)
                            findNavController().navigate(
                                R.id.action_loginFragment_to_verificationFragment,
                                bundle
                            )
                        }
                    } else {
                        showErrorAlert(result.message.toString())
                    }
                }
            }

            Status.ERROR -> {
                showErrorAlert(value.message.toString())
                if (value.message == "Your account is not verified please verify your account.") {
                    /*   val result = Gson().fromJson(
                           (value as JsonElement).toString(),
                           LoginResponse::class.java
                       )
                       val bundle = Bundle()
                       bundle.putString("countryCode", result.body.country_code)
                       bundle.putString("phone", result.body.phone_number)*/
                    /*findNavController().navigate(
                        R.id.action_loginFragment_to_verificationFragment,
                        bundle
                    )*/
                }

            }

            else -> {}
        }
    }

    override fun onSuccessGoogle(account: GoogleSignInAccount?) {
        socialMap["social_type"] = 2
        account?.run {
            id?.let {
                socialMap["social_id"] = it
                socialMap["googleId"] = it
            }
            email?.let {
                socialMap["email"] = it
            }
//            map["role_type"] = role
            photoUrl?.let {
                socialMap["image"] = it
            }
            displayName?.let {
                socialMap["name"] = it
//                val splited: List<String> = it.split(" ")
//                map["first_name"] = splited[0]
//                map["last_name"] = splited[1]//crash in case of only first name
            }
            givenName?.let {
                socialMap["nick_name"] = it
            }
        }
        for ((key, value) in socialMap)
            Log.d("knkwefnwklefn", "onSuccessGoogle: $key = $value")
        val map2 = java.util.HashMap<String, String>()
        map2["social_id"] = socialMap["social_id"].toString()
        map2["social_type"] = socialType   //1= google
        appViewModel.postApi(CHECK_SOCIAL_LOG_IN, map2, requireContext(), true)
            .observe(viewLifecycleOwner, this)

    }

    override fun onErrorGoogle(error: String) {
        showErrorAlert(error.toString())
    }

}
