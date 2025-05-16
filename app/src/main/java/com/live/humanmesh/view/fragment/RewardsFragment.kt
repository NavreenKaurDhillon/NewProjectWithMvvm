package com.live.humanmesh.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.util.CoilUtils.result
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.databinding.FragmentRewardsBinding
import com.live.humanmesh.model.Reward
import com.live.humanmesh.model.RewardsListResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants.REWARDS_LIST
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.view.adapter.RewardsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RewardsFragment : Fragment(), Observer<Resource<JsonElement>>  {

    private lateinit var binding: FragmentRewardsBinding
    private lateinit var rewardsAdapter: RewardsAdapter
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { binding = FragmentRewardsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        binding.toolBar.tvTitle.text = getString(R.string.rewards)
        binding.toolBar.ivBack.setOnClickListener { findNavController().popBackStack() }
        hitRewardsList()
    }

    private fun hitRewardsList() {
        appViewModel.getApi(REWARDS_LIST, requireContext(), true).observe(viewLifecycleOwner, this)
    }

    private fun setAdapter(body: List<Reward>) {
        rewardsAdapter = RewardsAdapter(body)
        binding.rvRewards.adapter = rewardsAdapter
    }

    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == REWARDS_LIST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        RewardsListResponse::class.java
                    )
                    if (result.code == 200) {
                        if (result.body.isNotEmpty())
                            setAdapter(result.body)
                        else
                            binding.noDataTV.visible()
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