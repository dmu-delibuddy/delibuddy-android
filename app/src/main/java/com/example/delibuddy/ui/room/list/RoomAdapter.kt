package com.example.delibuddy.ui.room.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.delibuddy.R
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.databinding.ItemRoomBinding
import java.text.SimpleDateFormat
import java.util.Locale

/*
    * 홈 화면 방 목록 어댑터. (카드 : item_room.xml)
    * DB 의 enum 은 영어 그대로 두고 화면에 보일 때만 한글로 변환
    * 카드를 누르면 onClick 으로 roomId 를 넘김
*/

class RoomAdapter (
    private val onClick : (roomId : String) -> Unit
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

    inner class RoomViewHolder (
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // 방 정보 → 카드 화면 (배지, 마감, 배달비, 제목, 태그) + 클릭 연결
        fun bind(room : RoomModel) = with(binding) {
            // 모집 배지 = 상태 + 인원 / 마감 시간
            textStatus.text = "${statusLabel(room.status)} ${room.currentCount}/${room.maxParticipants}명"
            textDeadline.text = room.deadline
                ?.let { "${TIME_FORMAT.format(it.toDate())} 마감" }
                ?: "시간 제한 없음"

            // 배달비
            textDeliveryFee.text = "배달비 1인당 %,d원".format(room.deliveryFeePerPerson)

            // 제목
            textTitle.text = room.title

            // 메뉴 태그 , 장소 태그 / 모드 배지
            textMenu.text = "메뉴: ${room.restaurant}"
            textPlace.visibility = View.GONE // 장소 필드가 정해지면 표시로 바꿀 예정
            textMode.setText(modeLabel(room.mode))

            root.setOnClickListener { onClick(room.id) }
        }
    }

    companion object {
        // 예 : 오후 6:30
        private val TIME_FORMAT = SimpleDateFormat("a h:mm", Locale.KOREA)

        private val DIFF = object : DiffUtil.ItemCallback<RoomModel>() {
            // 같은 방인지 문서 id로 판단
            override fun areItemsTheSame(old: RoomModel, new: RoomModel) = old.id == new.id

            // 내용이 같은지 data class 의 equals (모든 필드 비교)
            override fun areContentsTheSame(old: RoomModel, new: RoomModel) = old == new
        }
    }

    // 방 상태 → 카드 표시 문구
    private fun statusLabel(status: RoomStatus) = when (status) {
        RoomStatus.RECRUITING -> "모집 중"
        RoomStatus.FULL -> "정원 마감"
        RoomStatus.ORDER_CONFIRMED -> "모집 마감"
        RoomStatus.COMPLETED -> "완료"
    }

    // 모드 → 카드 표시 문구 (픽업만 / 밥친구, 홈 / 상세와 같은 문구)
    @StringRes
    private fun modeLabel(mode: RoomMode) = when (mode) {
        RoomMode.PICKUP_ONLY -> R.string.mode_pickup
        RoomMode.MEAL_BUDDY -> R.string.mode_meal_buddy
    }

    // 카테고리 → 표시 문구
    private fun categoryLabel(category: FoodCategory) = when (category) {
        FoodCategory.KOR -> "한식"
        FoodCategory.CHN -> "중식"
        FoodCategory.JPN -> "일식"
        FoodCategory.WEST -> "양식"
        FoodCategory.ETC -> "기타"
    }
}