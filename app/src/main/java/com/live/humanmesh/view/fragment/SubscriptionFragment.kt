package com.live.humanmesh.view.fragment

import android.annotation.SuppressLint
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
import com.live.humanmesh.databinding.FragmentSubscriptionBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants.ADD_SUBSCRIPTION
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.adapter.SubscriptionsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionFragment : Fragment(), Observer<Resource<JsonElement>> {

    lateinit var binding: FragmentSubscriptionBinding
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        initClickListener()
        setAdapter()
    }

    private fun buySubscription(planId: String) {
        val hashMap = HashMap<String, String>()
        hashMap["plan_id"] = planId
        appViewModel.postApi(ADD_SUBSCRIPTION, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun initClickListener() {
        binding.apply {
            toolBar.ivBack.visible()
            toolBar.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolBar.tvTitle.text = requireActivity().getString(R.string.subscription)
        }
    }

    @SuppressLint("ResourceType")
    private fun setAdapter() {
        val stringList = ArrayList<SubscriptionModel>()

        stringList.add(SubscriptionModel(1, "25 Words", "/\$10", R.color.light_blue))
        stringList.add(SubscriptionModel(2, "50 Words", "/\$30", R.color.light_red))
        stringList.add(SubscriptionModel(3, "100 Words", "/\$50", R.color.light_yellow))
        stringList.add(SubscriptionModel(4, "200 Words", "/\$80", R.color.light_green))

        val adapter = SubscriptionsAdapter(requireActivity(), stringList)
        binding.rvList.adapter = adapter
        adapter.clickListener = {
            buySubscription(it)
        }
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == ADD_SUBSCRIPTION) {
                      val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                          CommonResponse::class.java
                    )
                    if (result.code == 200) {
                        showSuccessAlert(result.message)
                        findNavController().popBackStack()
                    }
                }
            }
            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }
            else -> {}
        }
    }

    class SubscriptionModel(val id: Int, val plan: String, val price: String, val bgColor: Int)

}