package com.live.humanmesh.view.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.humanmesh.databinding.ItemWalkthroughBinding
import com.live.humanmesh.model.WalkthroughData


class ViewPagerAdapter(val context : Context, var dataList:ArrayList<WalkthroughData>): RecyclerView.Adapter<ViewPagerAdapter.HomeViewHolder>()
    {


    inner class HomeViewHolder(private val binding: ItemWalkthroughBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
                    binding.titleTV.text = dataList[pos].title
                    binding.descriptionTV.text = dataList[pos].description
                    Glide.with(context).load(dataList[pos].image).into(binding.imageView)
                }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ItemWalkthroughBinding.inflate(
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
