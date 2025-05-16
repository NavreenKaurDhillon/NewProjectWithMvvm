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
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.FragmentProfileBinding
import com.live.humanmesh.model.ContactUsResponse
import com.live.humanmesh.model.ProfileResponse
import com.live.humanmesh.model.UserProfileBody
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() , Observer<Resource<JsonElement>>{

    private lateinit var binding: FragmentProfileBinding
    private lateinit var appViewModel: AppViewModel
    private var userDetails :UserProfileBody?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        binding.toolBar.ivBack.gone()
        binding.toolBar.tvTitle.text= getString(R.string.profile)
        hitGetProfile()
        clickListeners()
    }

    private fun hitGetProfile(){
        if (getFromDatabase(Constants.USER_GENDER, "") == "0" )
            binding.imageView.setImageDrawable(requireContext().getDrawable(R.drawable.gray_male))
        else
            binding.imageView.setImageDrawable(requireContext().getDrawable(R.drawable.gray_female))

        appViewModel.getApi(Constants.GET_PROFILE,  requireContext(), true)
            .observe(viewLifecycleOwner, this)

    }

    private fun clickListeners() {
        binding.apply {
            editBT.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("name" , userDetails?.name)
                bundle.putString("nick_name" , userDetails?.nick_name)
                bundle.putString("location" , userDetails?.location)
                bundle.putString("dob" , userDetails?.dob)
                bundle.putString("email" , userDetails?.email)
                bundle.putString("phone" , userDetails?.phone_number)
                bundle.putString("country_code" , userDetails?.country_code.toString())
                bundle.putString("latitude" , userDetails?.latitude)
                bundle.putString("longitude" , userDetails?.longitude)
                findNavController().navigate(R.id.editProfileFragment, bundle)
            }
            myEventsBT.setOnClickListener {
                findNavController().navigate(R.id.myEventsFragment)
            }

        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when(value.status){
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.GET_PROFILE) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), ProfileResponse::class.java)
//                    1=>male,2=>female,3=>other
                    userDetails = result.body
                    if (result.body.gender == "0")
                        binding.imageView.setImageDrawable(requireContext().getDrawable(R.drawable.gray_male))
                     else
                        binding.imageView.setImageDrawable(requireContext().getDrawable(R.drawable.gray_female))
                   binding.userNameTV.text = result.body.name
                   binding.userEmailTV.text = result.body.email
                   binding.userPhoneTV.text = result.body.country_code+result.body.phone_number
                   binding.userLocationTV.text = result.body.location
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