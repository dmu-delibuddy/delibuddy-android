package com.example.delibuddy.ui.room.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryApp
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.databinding.ItemRoomBinding
import com.example.delibuddy.ui.room.deliveryAppLabel

/*
    * 홈 화면 방 목록 어댑터. (카드 : item_room.xml)
    * DB 의 enum 은 영어 그대로 두고 화면에 보일 때만 한글로 변환
    * 카드를 누르면 onClick 으로 roomId 를 넘김
*/

class RoomAdapter(
    private val onClick: (roomId: String) -> Unit
) : ListAdapter<RoomModel, RoomAdapter.RoomViewHolder>(DIFF) {

    // 방 카드 한 칸(item_room) 만들기
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    // position 번째 방 정보를 카드에 채우기
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomModel) = with(binding) {
            val context = root.context
            textStatus.setText(
                when (room.status) {
                    RoomStatus.RECRUITING -> if (room.isFull) {
                        R.string.detail_status_full
                    } else {
                        R.string.detail_status_recruiting
                    }
                    RoomStatus.ORDER_CONFIRMED -> R.string.detail_status_confirmed
                    RoomStatus.ORDERED -> R.string.detail_status_ordered
                    RoomStatus.RECEIVED -> R.string.detail_status_received
                    RoomStatus.COMPLETED -> R.string.detail_status_completed
                }
            )
            textParticipantCount.text = context.getString(
                R.string.chat_header_participant_count, room.currentCount, room.maxParticipants,
            )
            textDeliveryFee.text = "배달비 1인당 %,d원".format(room.deliveryFeePerPerson)
            imageDeliveryApp.setImageResource(
                when (room.deliveryApp) {
                    DeliveryApp.BAEMIN -> R.drawable.icon_delivery_baemin
                    DeliveryApp.COUPANG_EATS -> R.drawable.icon_delivery_coupang_eats
                    DeliveryApp.YOGIYO -> R.drawable.icon_delivery_yogiyo
                    DeliveryApp.OTHER -> R.drawable.receipt_icon
                }
            )
            textDeliveryApp.text = room.deliveryAppLabel(context)
            textTitle.text = room.title
            textRestaurant.text = room.restaurant
            textMode.root.displayedChild = if (room.mode == RoomMode.MEAL_BUDDY) 1 else 0

            root.setOnClickListener { onClick(room.id) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RoomModel>() {
            // 같은 방인지 문서 id로 판단
            override fun areItemsTheSame(old: RoomModel, new: RoomModel) = old.id == new.id

            // 내용이 같은지 data class 의 equals (모든 필드 비교)
            override fun areContentsTheSame(old: RoomModel, new: RoomModel) = old == new
        }
    }
}
