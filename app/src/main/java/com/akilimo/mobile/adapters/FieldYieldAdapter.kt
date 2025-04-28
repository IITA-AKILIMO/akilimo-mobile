package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.utils.TheItemAnimation.animate
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class FieldYieldAdapter(
    private val ctx: Context,
    private var items: List<FieldYield>,
    private val animationType: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var rowIndex = -1
    private val onAttach = true
    private var selectedYieldAmount = 0.0


    interface OnItemClickListener {
        fun onItemClick(view: View?, fieldYield: FieldYield?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mItemClickListener
    }

    fun setItems(selectedYieldAmount: Double, items: List<FieldYield>) {
        this.items = items
        this.selectedYieldAmount = selectedYieldAmount
        notifyDataSetChanged()
    }

    inner class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var rootYieldImage: ImageView =
            v.findViewById(R.id.rootYieldImage)
        var name: TextView = v.findViewById(R.id.name)
        var layoutView: View = v.findViewById(R.id.lyt_parent)
        var mainCard: CardView = v.findViewById(R.id.mainCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_recommendation_image, parent, false)
        viewHolder = OriginalViewHolder(view)
        return viewHolder
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {

            val fieldYield = items[position]
            val currentYieldAmount = fieldYield.yieldAmount
            holder.name.text = fieldYield.fieldYieldLabel
            displayImageOriginal(ctx, holder.rootYieldImage, fieldYield.imageId)

            holder.layoutView.setOnClickListener { view1: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view1, items[position], position)
                }
            }

            if ((rowIndex == position) || (currentYieldAmount == selectedYieldAmount)) {
                holder.mainCard.setCardBackgroundColor(ctx.resources.getColor(R.color.green_100))
            } else {
                holder.mainCard.setCardBackgroundColor(ctx.resources.getColor(R.color.grey_3))
            }

            setAnimation(holder.itemView, position)
        }
    }

    fun setActiveRowIndex(position: Int) {
        rowIndex = position
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }
}
