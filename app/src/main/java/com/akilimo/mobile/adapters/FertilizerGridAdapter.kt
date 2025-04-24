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
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.utils.Tools

class FertilizerGridAdapter(
    private val context: Context
) : RecyclerView.Adapter<FertilizerGridAdapter.OriginalViewHolder>() {

    private var items: List<Fertilizer> = ArrayList()
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null
    private var rowIndex = -1

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: Fertilizer, position: Int)
    }

    interface OnLoadMoreListener {
        fun onLoadMore(currentPage: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.mOnItemClickListener = listener
    }

    fun setItems(fertilizerList: List<Fertilizer>) {
        this.items = fertilizerList
        notifyDataSetChanged()
    }

    fun setActiveRowIndex(position: Int) {
        rowIndex = position
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
        this.onLoadMoreListener = listener
    }

    fun getAll(): List<Fertilizer> = items

    fun getSelected(): List<Fertilizer> {
        return items.filter { it.selected }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_fertilizer_grid_row, parent, false)
        return OriginalViewHolder(view)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val fertilizer = items[position]
        val fertilizerName = fertilizer.name
        val bagPrice = fertilizer.priceRange
        val isSelected = fertilizer.selected

        holder.fertilizerName.text = fertilizerName
        holder.bagPrice.text = if (isSelected) bagPrice else null

        holder.lytParent.setOnClickListener { clickListener(it, fertilizer, position) }
        Tools.displayImageOriginal(context, holder.image, R.drawable.ic_fertilizer_bag)

        val backgroundColor = if (isSelected) {
            context.resources.getColor(R.color.green_200)
        } else {
            context.resources.getColor(R.color.grey_5)
        }

        holder.lytParent.setCardBackgroundColor(backgroundColor)
    }

    private fun clickListener(view: View, fertilizer: Fertilizer, position: Int) {
        mOnItemClickListener?.onItemClick(view, fertilizer, position)
    }

    override fun getItemCount(): Int = items.size

    inner class OriginalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.fertilizerImage)
        val fertilizerName: TextView = view.findViewById(R.id.fertilizerName)
        val bagPrice: TextView = view.findViewById(R.id.bagPrice)
        val lytParent: CardView = view.findViewById(R.id.lyt_parent)
    }
}
