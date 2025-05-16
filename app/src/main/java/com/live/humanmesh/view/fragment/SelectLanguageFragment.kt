package com.live.humanmesh.view.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.live.humanmesh.R
import com.live.humanmesh.databinding.FragmentSelectLanguageBinding

class SelectLanguageFragment : Fragment() {

    private lateinit var binding: FragmentSelectLanguageBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
    }

    private fun clickListeners() {
        binding.apply {
            englishBT.setOnClickListener {
                englishBT.backgroundTintList = null
                englishBT.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                findNavController().navigate(R.id.action_selectLanguageFragment_to_loginFragment)
            }
            portuguesBT.setOnClickListener {
                portuguesBT.setTextColor(ResourcesCompat.getColor(resources, R.color.black, null))
                englishBT.backgroundTintList= ColorStateList(arrayOf(intArrayOf()), intArrayOf(ResourcesCompat.getColor(resources, R.color.white, null)))
                portuguesBT.backgroundTintList = null
                portuguesBT.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                findNavController().navigate(R.id.action_selectLanguageFragment_to_loginFragment)
            }
        }
    }
}