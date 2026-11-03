package com.example.delibuddy.ui.room.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod

/*
    * 방 만들기 1 ~ 4 단계 상태.
    * 입력값도 앞으로 여기에 모아서 이전 으로 돌아가도 값이 유지되게 함.
*/

class CreateRoomViewModel : ViewModel() {

    private val _step = MutableLiveData(1)
    val step: LiveData<Int> = _step
    // 1단계 : 방 정보
    private val _title = MutableLiveData("")
    val title : LiveData<String> = _title

    private val _maxParticipants = MutableLiveData(DEFAULT_PARTICIPANTS)
    val maxParticipants : LiveData<Int> = _maxParticipants

    private val _mode = MutableLiveData(RoomMode.PICKUP_ONLY)
    val mode : LiveData<RoomMode> = _mode

    fun setTitle(value: String) { _title.value = value }
    fun setMode(value: RoomMode) { _mode.value = value }

    fun increaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n < MAX_PARTICIPANTS) _maxParticipants.value = n + 1
    }

    fun decreaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n > MIN_PARTICIPANTS) _maxParticipants.value = n - 1
    }

    // 2단계 : 음식 정보
    private val _category = MutableLiveData<FoodCategory?>(null) // null = 아직 선택 안 함
    val category : LiveData<FoodCategory?> = _category

    private val _restaurant = MutableLiveData("")
    val restaurant : LiveData<String> = _restaurant

    fun setCategory(value : FoodCategory?) { _category.value = value }
    fun setRestaurant(value : String) { _restaurant.value = value }


    // 3단계 : 장소 , 마감 시간
    // 마감 시각을 하루 중 몇 분째인지로 저장 (18:30 -> 1110). null = 아직 선택 안 함
    private val _deadlineMinutes = MutableLiveData<Int?>(null)
    val deadlineMinutes : LiveData<Int?> = _deadlineMinutes

    private val _noTimeLimit = MutableLiveData(false)
    val noTimeLimit : LiveData<Boolean> = _noTimeLimit

    // 4단계 : 금액, 정산
    private val _minOrderAmount = MutableLiveData<Int?>(null) // null = 입력 안 함
    val minOrderAmount : LiveData<Int?> = _minOrderAmount

    private val _deliveryFee = MutableLiveData<Int?>(null)
    val deliveryFee : MutableLiveData<Int?> = _deliveryFee

    private val _foodCostSplit = MutableLiveData(FoodCostSplitMethod.PROPORTIONAL_SPLIT) // 개인 메뉴
    val foodCostSplit : LiveData<FoodCostSplitMethod> = _foodCostSplit

    private val _deliveryFeeSplit = MutableLiveData(DeliveryFeeSplitMethod.EQUAL_SPLIT) // 1/N
    val deliveryFeeSplit : LiveData<DeliveryFeeSplitMethod> = _deliveryFeeSplit

    fun setMinOrderAmount (value : Int?) { _minOrderAmount.value = value }
    fun setDeliveryFee (value : Int?) { _deliveryFee.value = value }
    fun setFoodCostSplit (value : FoodCostSplitMethod) { _foodCostSplit.value = value }
    fun setDeliveryFeeSplit (value : DeliveryFeeSplitMethod) { _deliveryFeeSplit.value = value }

    fun setDeadline (hour : Int, minute : Int) { _deadlineMinutes.value = hour * 60 + minute }
    fun setNoTimeLimit (value : Boolean) { _noTimeLimit.value = value }

    fun next() {
        val s = _step.value ?: 1
        if (s < LAST_STEP) _step.value = s + 1
    }

    fun prev() {
        val s = _step.value ?: 1
        if (s > 1) _step.value = s - 1
    }

    companion object {
        const val LAST_STEP = 4
        const val TITLE_MAX = 30
        const val MIN_PARTICIPANTS = 2 // 방장 포함
        const val MAX_PARTICIPANTS = 6
        const val DEFAULT_PARTICIPANTS = 3 // 기본값 설정
        const val RESTAURANT_MAX = 30
    }
}