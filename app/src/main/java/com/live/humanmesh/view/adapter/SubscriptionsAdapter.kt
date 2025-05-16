package com.live.humanmesh.view.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.R
import com.live.humanmesh.databinding.ItemSubscriptionBinding
import com.live.humanmesh.view.fragment.SubscriptionFragment.SubscriptionModel


class SubscriptionsAdapter(var context: Context, val stringList: ArrayList<SubscriptionModel>) :
    RecyclerView.Adapter<SubscriptionsAdapter.HomeViewHolder>() {
    var clickListener : ((id : String) -> Unit)?=null
    inner class HomeViewHolder(val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ItemSubscriptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.binding.apply {
            tvPrice.text = stringList[position].price
            tvPlan.text = stringList[position].plan


            // Set background color
            val drawableCV = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, stringList[position].bgColor)) // Set background color
                cornerRadius = 30f // Set corner radius
            }

       // Apply the drawable as background
            cardCv.background = drawableCV

            // Get the background as a GradientDrawable
            val drawable = tvBtn.background.mutate() as? GradientDrawable

// Change the stroke color dynamically
            drawable?.setStroke(2, ContextCompat.getColor(context, R.color.light_blue_border))

        // Set the stroke color dynamically
            if (position == 1) {
//                tvBtn.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_red_border)
                drawable?.setStroke(2, ContextCompat.getColor(context, R.color.light_red_border))
                tvBtn.setTextColor(ContextCompat.getColor(context, R.color.light_red_border))
            } else if (position == 2) {
                drawable?.setStroke(2, ContextCompat.getColor(context, R.color.light_yellow_border))
                tvBtn.setTextColor(ContextCompat.getColor(context, R.color.light_yellow_border))
            } else if (position == 3) {
                drawable?.setStroke(2, ContextCompat.getColor(context, R.color.light_green_border))
                tvBtn.setTextColor(ContextCompat.getColor(context, R.color.light_green_border))
            }
            tvBtn.setOnClickListener {
                clickListener?.invoke(stringList[position].id.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return stringList.size
    }
}
