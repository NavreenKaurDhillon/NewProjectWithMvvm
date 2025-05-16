package com.live.humanmesh.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.R
import com.live.humanmesh.databinding.MenuLayoutBinding
import com.live.humanmesh.databinding.RequestItemBinding
import com.live.humanmesh.model.EventBody
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.live.humanmesh.utils.AppUtils.toFormattedDate
import com.live.humanmesh.utils.AppUtils.toFormattedTime
import com.live.humanmesh.utils.Constants


class RequestsAdapter(private val events: List<EventBody>, private val type: String) :
    RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {
    var clickListener: ((type: String, eventId: String) -> Unit)? = null
    var eventId = ""

    class ViewHolder(val binding: RequestItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RequestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                locationTV.text = events[position].location
                titleTV.gone()
                Log.d("lkhnkjfwf", "onBindViewHolder: ${events[position]}")
                if (events[position].event_details!=null){
                    nameTV.text = events[position].event_details.title
                    descriptionTV.text = events[position].event_details.description
                    locationTV.text = events[position].event_details.location
//                    dateTV.text = events[position].event_details.createdAt.toFormattedDate()
//                    timeTV.text = events[position].event_details.createdAt.toFormattedTime()
                    if (events[position].event_details.type == "0") {
                        dateTV.gone()
                        clockIV.gone()
                        calendarIV.gone()
                        timeTV.gone()
                        realTimeTV.text = holder.itemView.context.getString(R.string.real_time)
                    }
                    else{
                        realTimeTV.text = holder.itemView.context.getString(R.string.afterwards)
                    }
                    Glide.with(holder.itemView.context)
                        .load(Constants.IMAGES_BASE_URL + events[position].event_details.image)
                        .into(userImageIV)
                }
                else {
                    nameTV.text = events[position].title
                    descriptionTV.text = events[position].description
                    dateTV.text = events[position].start_date?.toString()
                    timeTV.text = events[position].start_time?.toString()
                    Glide.with(holder.itemView.context)
                        .load(Constants.IMAGES_BASE_URL + events[position].image)
                        .into(userImageIV)
                    if (events[position].type == "0") {
                        dateTV.gone()
                        clockIV.gone()
                        calendarIV.gone()
                        timeTV.gone()
                        realTimeTV.text = holder.itemView.context.getString(R.string.real_time)
                    }
                    else{
                        realTimeTV.text = holder.itemView.context.getString(R.string.afterwards)
                    }

                }

                /*  val textsAdapter = TextsAdapter()
                  textRV.adapter = textsAdapter*/

                if (type == "mine") {
                    ivMenu.visible()
                    realTimeTV.gone()
                    acceptBT.gone()
                    rejectBT.gone()
                }



                baseLayout.setOnClickListener {
                  /*  if (events[position].requestDetails !=null && events[position].requestDetails.isNotEmpty())
                        eventId = events[position].requestDetails[0].id.toString()
                    else*/
                    if (type == "mine")
                        eventId = events[position].id.toString()
                    else
                        eventId = events[position].event_id.toString()
                    clickListener?.invoke("base",eventId)
                }
                acceptBT.setOnClickListener {
                   /* if (events[position].requestDetails !=null && events[position].requestDetails.isNotEmpty())
                        eventId = events[position].requestDetails[0].id.toString()
                    else*/
                    eventId = events[position].event_id.toString()
                    clickListener?.invoke("accept", eventId)
                }
                rejectBT.setOnClickListener {
                  /*  if (events[position].requestDetails !=null && events[position].requestDetails.isNotEmpty())
                        eventId = events[position].requestDetails[0].id.toString()
                    else*/
                        eventId = events[position].event_id.toString()
                    clickListener?.invoke("reject", eventId)
                }
                ivMenu.setOnClickListener {
                  /*  if (events[position].requestDetails !=null && events[position].requestDetails.isNotEmpty())
                        eventId = events[position].requestDetails[0].id.toString()
                    else*/
                        eventId = events[position].id.toString()
                    showPopUp(ivMenu, holder.itemView.context, eventId)
                }
            }
        }
    }

    private fun showPopUp(view: View, context: Context, eventId: String) {
        val binding = MenuLayoutBinding.inflate(LayoutInflater.from(context))

        val popupWindow = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.tvReport.text = context.getString(R.string.edit)
        binding.tvClearChat.text = context.getString(R.string.delete)
        binding.tvBlock.gone()
        binding.v2.gone()
        binding.tvReport.setOnClickListener {
            popupWindow.dismiss()
            clickListener?.invoke("edit", eventId)
        }
        binding.tvClearChat.setOnClickListener {
            popupWindow.dismiss()
            clickListener?.invoke("delete", eventId)
        }

        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.isOutsideTouchable = true
        binding.root.setOnClickListener {
            popupWindow.dismiss()

        }
        popupWindow.showAsDropDown(view)
    }
}