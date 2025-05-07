package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecommendationAdapter.RecViewHolder
import com.akilimo.mobile.mappers.ComputedResponse

class RecommendationAdapter : RecyclerView.Adapter<RecViewHolder>() {
    private var computedResponseList: List<ComputedResponse> = ArrayList()

    fun setData(computedResponseList: List<ComputedResponse>) {
        this.computedResponseList = computedResponseList
        notifyDataSetChanged()
    }


    override fun getItemCount() = computedResponseList.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.content_compute_card, viewGroup, false)
        return RecViewHolder(v)
    }

    override fun onBindViewHolder(recViewHolder: RecViewHolder, position: Int) {
        val cr = computedResponseList[position]
        recViewHolder.computedTitle.text = cr.computedTitle
        recViewHolder.computedBody.text = cr.computedRecommendation
    }

    class RecViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: CardView = itemView.findViewById(R.id.cv)
        var computedTitle: TextView = itemView.findViewById(R.id.recLabel)
        var computedBody: TextView = itemView.findViewById(R.id.recSubLabel)
    }
}
