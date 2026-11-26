package com.example.delibuddy.ui.chat.order

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.delibuddy.R
import com.example.delibuddy.databinding.ItemOrderSheetBinding
import com.example.delibuddy.ui.common.loadImage

class OrderSheetAdapter(
    private val onPhotoClick: (String) -> Unit,
) : ListAdapter<OrderSheetRow, OrderSheetAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemOrderSheetBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onPhotoClick,
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    class Holder(
        private val binding: ItemOrderSheetBinding,
        private val onPhotoClick: (String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: OrderSheetRow) = with(binding) {
            val context = root.context
            textHostBadge.isVisible = row.isHost
            textNickname.text = row.nickname.ifBlank { context.getString(R.string.chat_sender_participant) }

            val order = row.order
            textNoOrder.isVisible = order == null
            layoutOrder.isVisible = order != null

            val url = order?.imageUrl?.takeIf { it.isNotBlank() }
            imageCart.isVisible = url != null
            if (url != null) {
                imageCart.loadImage(url)
                imageCart.setOnClickListener { onPhotoClick(url) }
            }

            val memo = order?.memo?.takeIf { it.isNotBlank() }
            textMemo.isVisible = memo != null
            textMemo.text = memo

            textFoodShare.text = context.orderAmountText(row.foodShare)
            textDeliveryShare.text = context.orderAmountText(row.deliveryShare)
            textAmount.text = context.orderAmountText(row.amount)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OrderSheetRow>() {
            override fun areItemsTheSame(oldItem: OrderSheetRow, newItem: OrderSheetRow) =
                oldItem.userId == newItem.userId

            override fun areContentsTheSame(oldItem: OrderSheetRow, newItem: OrderSheetRow) =
                oldItem == newItem
        }
    }
}

fun Context.orderAmountText(amount: Long?): String =
    if (amount == null) getString(R.string.order_sheet_custom)
    else getString(R.string.room_detail_won, amount)
