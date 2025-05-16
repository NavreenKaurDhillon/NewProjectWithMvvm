package com.live.humanmesh.view.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.toDrawable
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
import com.live.humanmesh.databinding.DialogAccountCreatedSuccessfullyBinding
import com.live.humanmesh.databinding.FragmentVerificationBinding
import com.live.humanmesh.model.LoginResponse
import com.live.humanmesh.model.ResendOtpRespone
import com.live.humanmesh.model.VerifyOtpResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.LOG_IN
import com.live.humanmesh.utils.Constants.RESEND_OTP
import com.live.humanmesh.utils.Constants.VERIFY_OTP
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.activity.HomeActivity
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationFragment : Fragment(), Observer<Resource<JsonElement>> {

    private lateinit var binding: FragmentVerificationBinding
    private lateinit var appViewModel: AppViewModel
    private var countDownTimer: CountDownTimer? = null
    private val totalTimeInMillis = 30_000L // 30 seconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVerificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        try {
            startResendCodeTimer()
        }
        catch (e: Exception){

        }
        initClickListener()
    }

    private fun verifyCode() {
        val hashMap = HashMap<String, String>()
        hashMap["otp"] = binding.pinview.text.toString()
        appViewModel.postApi(VERIFY_OTP, hashMap, requireContext(), true).observe(viewLifecycleOwner, this)
    }

    private fun initClickListener() {
        binding.apply {
            toolBar.tvTitle.text = requireActivity().getString(R.string.otp_verification)
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            verifyBT.setOnClickListener {
                if (binding.pinview.text.toString().trim().isEmpty()) {
                    showErrorAlert(resources.getString(R.string.please_enter_otp))
                } else if (binding.pinview.text.toString().length != 4) {
                    showErrorAlert(resources.getString(R.string.please_enter_otp))
                } else {
                    verifyCode()
                }
            }
            tvResend.setOnClickListener {
                hitResendCode()
            }
        }
    }

    private fun hitResendCode() {
        val hashMap = HashMap<String, String>()
        hashMap["country_code"] = getFromDatabase(Constants.COUNTRY_CODE, "")
        hashMap["phone_number"] = getFromDatabase(Constants.USER_PHONE, "")
        appViewModel.putWithParam(RESEND_OTP, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
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

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == VERIFY_OTP) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), VerifyOtpResponse::class.java)
                    if (result.code == 200) {
                        dialogAccountCreated()
                    }
                }
                if (value.apiEndpoint == RESEND_OTP) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), ResendOtpRespone::class.java)
                    if (result.code == 200) {
                      showSuccessAlert(result.message)
                    }
                }
            }
            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }
            else -> {}
        }
    }

    private fun startResendCodeTimer() {
        binding.phoneTV.text = getFromDatabase(Constants.COUNTRY_CODE, "")+getFromDatabase(Constants.USER_PHONE, "")
        binding.tvTime.isEnabled = false
        binding.tvTime.setTextColor(getColor(requireContext(), R.color.black_text)) // Disabled style
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTime.text = "$seconds"
            }
            override fun onFinish() {
                binding.tvTime.text = ""
                binding.tvResend.isEnabled = true
                binding.ivTime.gone()
                binding.tvResend.setTextColor(getColor(requireContext(), R.color.black_text)) // Active style
            }
        }.start()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}