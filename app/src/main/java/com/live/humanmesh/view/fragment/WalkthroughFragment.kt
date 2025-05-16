package com.live.humanmesh.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences
import com.live.humanmesh.databinding.FragmentWalkthroughBinding
import com.live.humanmesh.model.WalkthroughData
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.view.adapter.ViewPagerAdapter
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class WalkthroughFragment : Fragment() {
    private lateinit var binding : FragmentWalkthroughBinding
    private val dataList = ArrayList<WalkthroughData>()
    private val viewHolder by lazy { ViewPagerAdapter(requireContext(), dataList) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalkthroughBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        setUpViewPager()
    }

    private fun clickListeners() {
        binding.nextBT.setOnClickListener {
            var pos: Int = binding.viewPager.currentItem
            pos += 1
            binding.viewPager.currentItem = pos
            viewHolder.notifyDataSetChanged()
            if (pos == 2) {
                AppSharedPreferences.saveTutorialIntoDatabase(Constants.IS_TUTORIAL_SHOWN, true)
                findNavController().navigate(R.id.action_walkthroughFragment_to_selectLanguageFragment)
            }
        }
        binding.skipBT.setOnClickListener {
            findNavController().navigate(R.id.action_walkthroughFragment_to_selectLanguageFragment)
        }
    }

    private fun setUpViewPager() {
        // home viewpager adapter
        dataList.clear()
        dataList.add(WalkthroughData( getString(R.string.dummy_title), getString(R.string.dummy_para),R.drawable.tutorial1))
        dataList.add(WalkthroughData(getString(R.string.dummy_title), getString(R.string.dummy_para),R.drawable.wakthrough2))
//        dataList.add(WalkthroughData( getString(R.string.dummy_title), getString(R.string.dummy_para), R.drawable.walkthrough3))
        binding.viewPager.adapter = viewHolder
        binding.indicatorView.setupWithViewPager(binding.viewPager)
        binding.indicatorView.apply {
            setSliderColor(Color.parseColor("#33000000"), Color.parseColor("#FF000000"))
            setSliderWidth(resources.getDimension(com.intuit.sdp.R.dimen._22sdp))
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._4sdp))
            setSlideMode(IndicatorSlideMode.SMOOTH)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setPageSize(binding.viewPager.adapter!!.itemCount)
            notifyDataChanged()
        }
    }
}