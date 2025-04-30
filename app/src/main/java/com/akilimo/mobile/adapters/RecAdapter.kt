package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.VectorDrawableUtils

class RecAdapter(private val items: List<RecommendationOptions>) :
    RecyclerView.Adapter<RecAdapter.OriginalViewHolder>() {
    private lateinit var mLayoutInflater: LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = ItemCardRecommendationArrowBinding.inflate(mLayoutInflater, parent, false)
        return OriginalViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recModel = items[position]
        holder.bind(recModel, position)
    }

    override fun getItemCount() = items.size


    inner class OriginalViewHolder(itemView: ItemCardRecommendationArrowBinding, viewType: Int) :
        RecyclerView.ViewHolder(itemView.root) {
        var image: ImageView = itemView.statusImage
        var name: TextView = itemView.name
        var cardView: View = itemView.lytParent

        fun bind(recModel: RecommendationOptions, position: Int) {
            name.text = recModel.recName
            var icon = R.drawable.ic_info
            var statusColor = ContextCompat.getColor(itemView.context, R.color.red_400)
            if (recModel.adviceStatus!!.completed) {
                icon = R.drawable.ic_done
                statusColor = ContextCompat.getColor(itemView.context, R.color.green_600)
            }
            val drawable = VectorDrawableUtils.getDrawable(itemView.context, icon, statusColor)
            image.setImageDrawable(drawable)
        }
    }

}