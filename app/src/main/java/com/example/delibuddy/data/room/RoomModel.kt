package com.example.delibuddy.data.room

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint

// 방 상태 : 모집 중 -> 정원 마감 -> 모집 마갑(주문 확정) -> 완료
enum class RoomStatus { RECRUITING, FULL, ORDER_CONFIRMED, COMPLETED }

// 방 모드 : 픽업 / 밥친구
enum class RoomMode { PICKUP_ONLY, MEAL_BUDDY }

// 음식 카테고리 : 한식 / 중식 / 일식 / 양식 / 기타
enum class FoodCategory { KOR, CHN, JPN, WEST, ETC }

// 음식값 정산 방법 : 방장 부담 / 1/N / 개인 메누 / 직접 입력
enum class FoodCostSplitMethod { HOST_PAYS, EQUAL_SPLIT, PROPORTIONAL_SPLIT, CUSTOM }

// 배달비 정산 방법 : 방장 부담 / 1/N / 직접 입력
enum class DeliveryFeeSplitMethod { HOST_PAYS, EQUAL_SPLIT, CUSTOM }

/*
    * Firestore rooms / {roodId} 문서
    * toObject() 로 변환 위해 인자 없는 생성자가 필요 -> 모든 값에 기본값을 둔다.
    * enum : Firestore 에 이름 문자열로 저장됨.
*/

data class RoomModel (
    @DocumentId val id : String = "",
    val title: String = "",
    val hostId : String = "",
    val maxParticipants : Int = 2,
    val participantIds : List<String> = emptyList(),
    val mode : RoomMode = RoomMode.PICKUP_ONLY,
    val category : FoodCategory = FoodCategory.ETC,
    val restaurant : String = "",
    val location : GeoPoint? = null,
    val deadline : Timestamp? = null,
    val minOrderAmount : Int = 0,
    val estimatedDeliveryFee : Int = 0,
    val foodCostSplitMethod : FoodCostSplitMethod = FoodCostSplitMethod.EQUAL_SPLIT,
    val deliveryFeeSplitMethod : DeliveryFeeSplitMethod = DeliveryFeeSplitMethod.EQUAL_SPLIT,
    val status : RoomStatus = RoomStatus.RECRUITING,
    val createdAt : Timestamp? = null,
) {
    // 화면에서만 쓰는 값. Firestore 에 저장 X

    @get:Exclude
    val currentCount : Int get() = participantIds.size

    // 1인당 예상 배달비 (현재 인원 기준, 최소 1명)
    @get:Exclude
    val deliveryFeePerPerson : Int get() = estimatedDeliveryFee / maxOf(currentCount, 1)

    @get:Exclude
    val isDeadLinePassed : Boolean get() = deadline?.let { it < Timestamp.now() } ?: false

    // 지금 새로 참여할 수 있는지
    @get:Exclude
    val isJoinable : Boolean
        get() = status == RoomStatus.RECRUITING && currentCount < maxParticipants && !isDeadLinePassed

    fun isHost(uid : String?) : Boolean = uid != null && uid == hostId
    fun isParticipant(uid : String?) : Boolean = uid != null && uid in participantIds
}