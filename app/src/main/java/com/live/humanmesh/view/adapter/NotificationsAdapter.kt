package com.live.humanmesh.view.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.databinding.ItemNotificationsBinding
import com.live.humanmesh.model.Notification
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.toFormattedDate
import com.live.humanmesh.utils.AppUtils.visible


class NotificationsAdapter(var context: Context,private val stringList: List<Notification>) :
    RecyclerView.Adapter<NotificationsAdapter.HomeViewHolder>() {

        var clickListener: ((String, String, String) -> Unit)? = null
    inner class HomeViewHolder(val binding: ItemNotificationsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ItemNotificationsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.binding.apply {
            if (stringList[position].notification_type==1)
                buttonsLL.visible()
            tvTitle.text = stringList[position].title
            tvMessage.text = stringList[position].message
            tvDate.text = stringList[position].createdAt.toFormattedDate()
            acceptBT.setOnClickListener {
                clickListener?.invoke("accept", stringList[position].id.toString(), stringList[position].sender_id.toString())
            }
            rejectBT.setOnClickListener {
                clickListener?.invoke("reject", stringList[position].id.toString(), stringList[position].sender_id.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return stringList.size
    }
}
