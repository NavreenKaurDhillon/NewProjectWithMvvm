package com.live.humanmesh.view.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
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
import com.live.humanmesh.database.AppSharedPreferences
import com.live.humanmesh.database.AppSharedPreferences.clearDatabase
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.FragmentSettingsBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.DELETE_ACCOUNT
import com.live.humanmesh.utils.Constants.LOGOUT
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.activity.AuthActivity
import com.live.humanmesh.view.adapter.SettingsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : Fragment() , Observer<Resource<JsonElement>>{
    private lateinit var appViewModel: AppViewModel

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        binding.toolBar.ivBack.gone()
        binding.toolBar.tvTitle.text= getString(R.string.settings)
        setSettingsAdapter()
    }

    private fun setSettingsAdapter() {
        val settingsAdapter = SettingsAdapter(resources.getStringArray(R.array.settingsList))
        binding.itemsRV.adapter = settingsAdapter
        settingsAdapter.clickListener = {
            when(it){
//                0 -> findNavController().navigate(R.id.notificationsFragment)
                1 -> findNavController().navigate(R.id.accountPrivacyFragment)
                2 -> findNavController().navigate(R.id.subscriptionFragment)
                3 -> shareApp()
                4 -> findNavController().navigate(R.id.rewardsFragment)
                5 -> {
                    val  bundle = Bundle()
                    bundle.putString("title", "Privacy Policy")
                    findNavController().navigate(
                        R.id.privacyPolicyFragment,
                        bundle
                    )
                }//privacy
                6 -> {
                    val  bundle = Bundle()
                    bundle.putString("title", "Terms & Conditions")
                    findNavController().navigate(R.id.privacyPolicyFragment, bundle)
                } //terms
                7 -> {
                    val  bundle = Bundle()
                    bundle.putString("title", "About Us")
                    findNavController().navigate(R.id.privacyPolicyFragment, bundle)
                } //abut us
                8 -> findNavController().navigate(R.id.faqFragment) //faq
                9 -> findNavController().navigate(R.id.contactUsFragment) //contact us
                10 -> deleteAccount()
                11 -> logout()
            }
        }
    }

    private fun shareApp(){
        val deepLink = Constants.DEEP_LINK_URL
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink)
        val message = getString(R.string.check_out_this_awesome_app_use_my_code_to_sign_up_to_words_for_free, getFromDatabase(Constants.USER_GENDER, ""))
        requireContext().startActivity(Intent.createChooser(shareIntent,message))
    }


    private fun logout() {
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
            ivTop.setImageResource(R.drawable.baseline_logout_24)
            tvHeading.text = getString(R.string.logout)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_logout)
            btnYes.setOnClickListener {
                dialog.dismiss()
                hitLogoutApi()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()

    }

    private fun hitLogoutApi() {
        appViewModel.postApi(LOGOUT, hashMapOf(), requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun hitDeleteAccountApi() {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = AppSharedPreferences.getFromDatabase(Constants.USER_ID,"")
        appViewModel.postApi(DELETE_ACCOUNT,hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun deleteAccount() {
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
            ivTop.setImageResource(R.drawable.delete_red)
            tvHeading.text = getString(R.string.delete_account)
            tvSubHeading.text = getString(R.string.are_you_sure_want_to_delete_this_account)
            btnYes.setOnClickListener {
                dialog.dismiss()
                hitDeleteAccountApi()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()

    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == LOGOUT) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        CommonResponse::class.java)
                    if (result.code == 200) {
                        clearDatabase()
                        startActivity(Intent(requireActivity(), AuthActivity::class.java))
                        requireActivity().finishAffinity()
                    }
                }
                if (value.apiEndpoint == DELETE_ACCOUNT) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(),
                        CommonResponse::class.java)
                    if (result.code == 200) {
                        clearDatabase()
                        saveIntoDatabase(Constants.IS_LOGIN, false)
                        showSuccessAlert(result.message)
                        startActivity(Intent(requireActivity(), AuthActivity::class.java))
                        requireActivity().finishAffinity()
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