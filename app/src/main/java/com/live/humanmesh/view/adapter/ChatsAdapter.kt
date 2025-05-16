package com.live.humanmesh.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.ChatItemBinding
import com.live.humanmesh.model.AllChatsResponse
import com.live.humanmesh.model.AllChatsResponseItem
import com.live.humanmesh.utils.AppUtils.maskString
import com.live.humanmesh.utils.AppUtils.toFormattedTime
import com.live.humanmesh.utils.Constants
import java.util.ArrayList

class ChatsAdapter(private val chats: ArrayList<AllChatsResponseItem>) :
    RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {
    var clickListener: ((receiverId: String) -> Unit)? = null

    class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(binding) {
                if (chats[position].profileVisitPermission == 1 ||chats[position].account_public=="1")
                    userNameTV.text = chats[position].receiverName
                else
                    userNameTV.text = maskString(chats[position].receiverName)
                if (chats[position].last_message.isNotEmpty() && chats[position].last_message != "")
                    messageTV.text = chats[position].last_message.toString()
                else
                    messageTV.text = holder.itemView.context.getString(R.string.no_messages)
                timeTV.text = chats[position].createdAt.toString().toFormattedTime()

                if ( chats[position].gender=="0")
                    userIV.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_male))
                else
                    userIV.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_female))

                baseLayout.setOnClickListener {
                    if (getFromDatabase(
                            Constants.USER_ID,
                            ""
                        ) == chats[position].senderId.toString()
                    )
                        clickListener?.invoke(chats[position].receiverId.toString())
                    else
                        clickListener?.invoke(chats[position].senderId.toString())
                }
            }
        }
    }
}