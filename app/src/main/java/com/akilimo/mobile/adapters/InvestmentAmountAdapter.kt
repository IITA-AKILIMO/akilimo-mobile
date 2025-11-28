package com.akilimo.mobile.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.DialogCustomAmountBinding
import com.akilimo.mobile.databinding.ItemInvestmentAmountBinding
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.utils.MathHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvestmentAmountAdapter(
    private val onItemClick: (InvestmentAmount, Double) -> Unit
) : ListAdapter<InvestmentAmount, InvestmentAmountAdapter.InvestmentViewHolder>(DiffCallback) {

    private var selectedInvestment: SelectedInvestment? = null
    private var selectedEnumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE
    private var selectedFarmSize: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvestmentViewHolder {
        val binding = ItemInvestmentAmountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvestmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvestmentViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = item.id == selectedInvestment?.investmentId
        holder.bind(item, isSelected)
        holder.itemView.setOnClickListener { holder.handleSelection(item) }
    }

    fun updateSelection(
        investment: SelectedInvestment,
        enumAreaUnit: EnumAreaUnit,
        farmSize: Double
    ) {
        val previous = selectedInvestment
        selectedInvestment = investment
        selectedEnumAreaUnit = enumAreaUnit
        selectedFarmSize = farmSize

        if (previous != null && previous != investment) {
            val oldIndex = currentList.indexOfFirst { it.id == previous.investmentId }
            if (oldIndex >= 0) notifyItemChanged(oldIndex)
        }
        val newIndex = currentList.indexOfFirst { it.id == investment.investmentId }
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }
    inner class InvestmentViewHolder(
        private val binding: ItemInvestmentAmountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InvestmentAmount, isSelected: Boolean) = with(binding) {
            val ctx = root.context
            val amount = item.investmentAmount
            val savedAmount = selectedInvestment?.chosenAmount
            val isExactAmount = selectedInvestment?.isExactAmount ?: false

            // Display amount based on type
            if (amount <= 0.0 && item.exactAmount) {
                tvAmountValue.text = ctx.getString(R.string.exact_investment_x_per_field_area)

                if (savedAmount != null && isExactAmount) {
                    tvAreaUnit.visibility = View.VISIBLE
                    tvAreaUnit.text = ctx.getString(R.string.lbl_investment_amount_simple)
                        .replace("{amount}", MathHelper.format(savedAmount))
                        .replace("{currency}", item.currencyCode)
                        .replace("{size}", MathHelper.format(selectedFarmSize))
                        .replace("{unit}", selectedEnumAreaUnit.label(ctx))
                } else {
                    tvAreaUnit.visibility = View.GONE
                }
            } else {
                tvAmountValue.text = ctx.getString(R.string.lbl_investment_per_area)
                    .replace("{amount}", MathHelper.format(amount))
                    .replace("{currency}", item.currencyCode)
                    .replace("{size}", MathHelper.format(selectedFarmSize))
                    .replace("{unit}", selectedEnumAreaUnit.label(ctx))
                tvAreaUnit.visibility = View.GONE
            }

            // Update selection background
            val bgColor = if (isSelected) {
                ContextCompat.getColor(ctx, R.color.color_focus)
            } else {
                Color.TRANSPARENT
            }
            root.setBackgroundColor(bgColor)
        }

        fun handleSelection(item: InvestmentAmount) {
            // Update previous selection UI
            val previous = selectedInvestment
            if (previous?.id != item.id) {
                val oldIndex = currentList.indexOfFirst { it.id == previous?.id }
                if (oldIndex >= 0) {
                    notifyItemChanged(oldIndex)
                }
                notifyItemChanged(bindingAdapterPosition)
            }

            // Handle amount selection
            val amount = item.investmentAmount
            if (amount <= 0.0) {
                showCustomAmountDialog(item)
            } else {
                onItemClick(item, amount)
            }
        }

        private fun showCustomAmountDialog(item: InvestmentAmount) {
            val ctx = binding.root.context
            val dialogBinding = DialogCustomAmountBinding.inflate(LayoutInflater.from(ctx))

            val dialog = MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.lbl_investment_amount)
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.lbl_ok, null)
                .setNegativeButton(R.string.lbl_cancel) { d, _ -> d.dismiss() }
                .create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                // Setup focus behavior
                dialogBinding.etCustomAmount.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                }

                positiveButton?.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                }

                negativeButton?.isFocusable = false

                // Handle OK button click with validation
                positiveButton?.setOnClickListener {
                    val input = dialogBinding.etCustomAmount.text.toString().trim()
                    val amount = input.toDoubleOrNull()

                    when {
                        amount == null || amount <= 0.0 -> {
                            dialogBinding.etCustomAmount.error =
                                ctx.getString(R.string.lbl_investment_amount_prompt)
                        }
                        else -> {
                            onItemClick(item, amount)
                            dialog.dismiss()
                        }
                    }
                }
            }

            dialog.show()
        }
    }
    private object DiffCallback : DiffUtil.ItemCallback<InvestmentAmount>() {
        override fun areItemsTheSame(
            oldItem: InvestmentAmount,
            newItem: InvestmentAmount
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: InvestmentAmount,
            newItem: InvestmentAmount
        ): Boolean =
            oldItem == newItem
    }
}
