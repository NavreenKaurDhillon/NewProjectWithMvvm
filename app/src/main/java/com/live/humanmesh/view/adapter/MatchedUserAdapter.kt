package com.live.humanmesh.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.R
import com.live.humanmesh.databinding.MatchedUserItemBinding
import com.live.humanmesh.model.BodyXXX
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.maskString

class MatchedUserAdapter(private val body: List<BodyXXX>) :
    RecyclerView.Adapter<MatchedUserAdapter.ViewHolder>() {

    var chatClickListener: ((userId: String) -> Unit)? = null

    class ViewHolder(val binding: MatchedUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchedUserAdapter.ViewHolder {
        val binding =
            MatchedUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchedUserAdapter.ViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                messageTV.gone()
                if (body[position].account_public == "1" || body[position].profileVisitPermission == 1)
                    userNameTV.text = body[position].name
                else
                    userNameTV.text = maskString(body[position].name)
//                  messageTV.text = body[position].senderDetail.
                if (body[position].gender == "0")
                    userIV.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_male))
                else
                    userIV.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.gray_female))


                baseLayout.setOnClickListener {
                    chatClickListener?.invoke(body[position].id.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return body.size
    }

}