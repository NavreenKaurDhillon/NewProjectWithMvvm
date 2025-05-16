package com.live.humanmesh.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.humanmesh.databinding.BannerItemBinding
import com.live.humanmesh.databinding.ItemWalkthroughBinding
import com.live.humanmesh.model.BannerItem
import com.live.humanmesh.utils.Constants

class BannerViewPager (val context: Context, var dataList: List<BannerItem>): RecyclerView.Adapter<BannerViewPager.HomeViewHolder>()
{


    inner class HomeViewHolder(private val binding: BannerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
            Glide.with(context).load(Constants.IMAGES_BASE_URL+dataList[pos].image).into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            BannerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}
