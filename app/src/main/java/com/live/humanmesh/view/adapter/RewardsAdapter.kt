package com.live.humanmesh.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.databinding.ItemRewardsBinding
import com.live.humanmesh.model.Reward

class RewardsAdapter(private val rewards: List<Reward>) : RecyclerView.Adapter<RewardsAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemRewardsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RewardsAdapter.ViewHolder {
        val binding =
            ItemRewardsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardsAdapter.ViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                messageTV.text = rewards[position].message
            }
        }
    }

    override fun getItemCount(): Int {
        return rewards.size
    }

}