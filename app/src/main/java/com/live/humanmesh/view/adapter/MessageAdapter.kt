package com.live.humanmesh.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.ItemChatListBinding
import com.live.humanmesh.model.Message
import com.live.humanmesh.utils.AppUtils.getChatTimeAgo
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.isToday
import com.live.humanmesh.utils.AppUtils.isYesterday
import com.live.humanmesh.utils.AppUtils.toFormattedDate
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants

class MessageAdapter(
    private val context: Context,
    private val msgList: List<Message>,
    private val receiverGender: String?
) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

        var senderSentImage =""
        var receiverSentImage =""
    var clickListener: ((type : String,userId: String) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding) {
            val currentDate = msgList[position].createdAt.toFormattedDate()
            val previousDate = if (position > 0) msgList[position - 1].createdAt.toFormattedDate() else null

            if (position == 0 || currentDate != previousDate) {
                linearLayout.visible()
                if (msgList[position].createdAt!="") {
                    dateTV.text = when {
                        isToday(msgList[position].createdAt) -> "Today"
                        isYesterday(msgList[position].createdAt) -> "Yesterday"
                        else -> currentDate
                    }
                }
            } else {
                linearLayout.gone()
            }
            ivProfile1.isEnabled = false
            ivProfile2.isEnabled = false

            /*     if (lastDate == "") {
                     lastDate = msgList[position].createdAt
                     linearLayout.visible()
                 } else
                     if (lastDate.toFormattedDate() == msgList[position].createdAt.toFormattedDate())
                         linearLayout.gone()
                     else
                         linearLayout.visible()*/
//            dateTV.text = lastDate
            if (msgList[position].senderId.toString() == getFromDatabase(Constants.USER_ID, "")) {
                viewReceived.gone()
                tvSendTime.text = getChatTimeAgo(msgList[position].createdAt, dateTV)
                if (getFromDatabase(Constants.USER_GENDER, "") == "0" )
                    ivProfile2.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_male))
                else
                    ivProfile2.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_female))
                if (msgList[position].thumbnail.isNotEmpty()) {
                    Glide.with(context).load(Constants.IMAGES_BASE_URL+msgList[position].thumbnail).into(sentIV)
                    sentIV.visible()
                    senderSentImage = Constants.IMAGES_BASE_URL+msgList[position].thumbnail
                }
                else
                    sentIV.gone()
                if (msgList[position].message.isNotEmpty()){
                    textView2.text = msgList[position].message
                    textView2.visible()
                }
                if (msgList[position].profileVisitPermission==1 || msgList[position].receiver_account_public=="1")
                    ivProfile2.isEnabled= true
            } else {
                viewSend.gone()
                tvReceivedTime.text = getChatTimeAgo(msgList[position].createdAt, dateTV)
                if ( receiverGender== "0")
                    ivProfile1.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_male))
                else
                    ivProfile1.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_female))
                if (msgList[position].thumbnail.isNotEmpty()) {
                    Glide.with(context).load(Constants.IMAGES_BASE_URL+msgList[position].thumbnail).into(sentIV2)
                    sentIV2.visible()
                    receiverSentImage = Constants.IMAGES_BASE_URL+msgList[position].thumbnail
                }
                else
                    sentIV2.gone()
                if (msgList[position].message.isNotEmpty()){
                    textView.text = msgList[position].message
                    textView.visible()
                }
                if (msgList[position].profileVisitPermission==1 || msgList[position].receiver_account_public=="1")
                    ivProfile1.isEnabled= true
            }

            /* if (showImages)
             {
                 sentIV2.visible()
                 sentIV.visible()
                 tvSendTime2.visible()
             }else{
                 sentIV2.gone()
                 sentIV.gone()
                 tvSendTime2.gone()
             }*/

            ivProfile1.setOnClickListener {
                val otherId = if (msgList[position].senderId.toString() == getFromDatabase(
                        Constants.USER_ID, ""))
                    msgList[position].receiverId
                else
                    msgList[position].senderId
                if (msgList[position].receiver_account_public =="1" || msgList[position].profileVisitPermission==1)
                clickListener?.invoke("details",otherId.toString())
            }

            sentIV.setOnClickListener {
                clickListener?.invoke("image",senderSentImage)
            }


            sentIV2.setOnClickListener {
                clickListener?.invoke("image",receiverSentImage)
            }


        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    class MyViewHolder(var binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}