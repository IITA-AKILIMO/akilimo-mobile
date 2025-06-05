package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ListFertilizerGridRowBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.utils.Tools

class FertilizerGridAdapter(
    private val context: Context
) : RecyclerView.Adapter<FertilizerGridAdapter.OriginalViewHolder>() {

    private var availableFertilizers: List<Fertilizer> = ArrayList()
    private var mOnItemClickListener: OnItemClickListener? = null
    private var rowIndex = -1

    interface OnItemClickListener {
        fun onItemClick(view: View, clickedFertilizer: Fertilizer, position: Int)
    }


    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.mOnItemClickListener = listener
    }

    fun setFertilizers(fertilizerList: List<Fertilizer>) {
        this.availableFertilizers = fertilizerList
        notifyDataSetChanged()
    }

    fun setFertilizer(fertilizer: Fertilizer, position: Int) {
        (availableFertilizers as MutableList)[position] = fertilizer
        notifyItemChanged(position)
    }

    fun setActiveRowIndex(position: Int) {
        rowIndex = position
    }


    fun getAll(): List<Fertilizer> = availableFertilizers

    private fun clickListener(view: View, fertilizer: Fertilizer, position: Int) {
        notifyItemChanged(position)
        mOnItemClickListener?.onItemClick(view, fertilizer, position)
    }

    override fun getItemCount(): Int = availableFertilizers.size

    inner class OriginalViewHolder(val binding: ListFertilizerGridRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListFertilizerGridRowBinding.inflate(inflater, parent, false)
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val fertilizer = availableFertilizers[position]
        val isSelected = fertilizer.selected

        with(holder.binding) {
            fertilizerName.text = fertilizer.name
            bagPrice.text = if (isSelected) fertilizer.priceRange else null

            lytParent.setOnClickListener { clickListener(it, fertilizer, position) }
            Tools.displayImageOriginal(context, fertilizerImage, R.drawable.ic_fertilizer_bag)

            val backgroundColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.green_200)
            } else {
                ContextCompat.getColor(context, R.color.grey_5)
            }

            lytParent.setCardBackgroundColor(backgroundColor)
        }
    }
}
