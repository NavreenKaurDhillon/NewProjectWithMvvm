package com.live.humanmesh.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.databinding.SettingsItemBinding
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible

class SettingsAdapter(private val list: Array<String>) :
    RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
         var clickListener :((pos : Int)-> Unit)?= null
    class ViewHolder(val binding: SettingsItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SettingsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                titleTV.text = list[position]
                if (position == 0) {
                    nextIV.gone()
                    switchCompat.visible()
                }
                if (position == list.size - 1) {
                    nextIV.gone()
                    switchCompat.gone()
                }
                baseLayout.setOnClickListener {
                    clickListener?.invoke(position)
                }
            }
        }
    }
}