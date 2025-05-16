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
import com.live.humanmesh.databinding.FragmentFaqBinding
import com.live.humanmesh.model.Faq
import com.live.humanmesh.model.FaqResponse
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.view.adapter.FaqAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FaqFragment : Fragment(), Observer<Resource<JsonElement>>{
    private lateinit var binding: FragmentFaqBinding
    private lateinit var appViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaqBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        hitFaqApi()
        clicks()
    }

    private fun clicks() {
        binding.toolbar.tvTitle.text = getString(R.string.faq_s)
        binding.toolbar.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private  fun hitFaqApi(){
        appViewModel.getApi(Constants.FAQS, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun setFaqAdapter(faqs: List<Faq>) {
       val adapter = FaqAdapter(requireContext(),faqs)
        binding.rvFaq.adapter = adapter
    }
    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.FAQS) {
                    val result = Gson().fromJson((value.data as JsonElement).toString(), FaqResponse::class.java)
                    setFaqAdapter(result.body)
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