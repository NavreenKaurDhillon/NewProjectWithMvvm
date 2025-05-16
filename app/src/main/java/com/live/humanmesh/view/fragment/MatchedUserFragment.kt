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
import com.live.humanmesh.databinding.FragmentMatchedUserBinding
import com.live.humanmesh.model.BodyXXX
import com.live.humanmesh.model.MatchedUsersResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.view.adapter.MatchedUserAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MatchedUserFragment : Fragment() , Observer<Resource<JsonElement>>{

    private lateinit var binding: FragmentMatchedUserBinding
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchedUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        binding.toolBar.ivBack.gone()
        binding.toolBar.tvTitle.text= getString(R.string.matched_user)
        getList()
        clickListeners()
    }

    private fun getList() {
        appViewModel.getApi(Constants.MATCHED_USERS, requireContext(), true).observe(viewLifecycleOwner, this)
    }

    private fun clickListeners() {
        binding.apply {

        }
    }

    private fun setUsersAdapter(body: List<BodyXXX>) {
        val matchedUserAdapter = MatchedUserAdapter(body)
        binding.usersRV.adapter = matchedUserAdapter
        matchedUserAdapter.chatClickListener={
            val bundle = Bundle()
            bundle.putString("receiverId", it)
            findNavController().navigate(R.id.matched_fragment_to_user_chat, bundle)
        }
    }
    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.MATCHED_USERS) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        MatchedUsersResponse::class.java
                    )
                    if (result.body.isEmpty())
                        binding.noDataTV.visible()
                    setUsersAdapter(result.body)
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