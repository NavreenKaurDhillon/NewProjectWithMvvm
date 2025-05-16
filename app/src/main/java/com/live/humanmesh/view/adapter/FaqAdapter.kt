package com.live.humanmesh.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.humanmesh.databinding.ItemFaqsBinding
import com.live.humanmesh.model.Faq
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible

class FaqAdapter(var context: Context, private val faqs: List<Faq>) : RecyclerView.Adapter<FaqAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(val binding: ItemFaqsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ItemFaqsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        with(holder.binding) {
            tvFaq.text = faqs[position].questions
            tvFaqAns.text = faqs[position].answer

            if (position == 0){
                tvFaqAns.visible()
                ivDropAns.gone()
                ivUpAns.visible()
            }

            ivDropAns.setOnClickListener {
                tvFaqAns.visible()
                ivDropAns.gone()
                ivUpAns.visible()
            }

            ivUpAns.setOnClickListener {
                tvFaqAns.gone()
                ivDropAns.visible()
                ivUpAns.gone()
            }

            tvFaq.text = "Question 0"+position+1
            tvFaqAns.text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been dummy text ever since the 1500s."
        }
    }

    override fun getItemCount(): Int {
        return faqs.size
    }
}
