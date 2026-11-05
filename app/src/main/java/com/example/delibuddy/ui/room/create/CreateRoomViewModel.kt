package com.example.delibuddy.ui.room.create

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomException
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar

/*
    * 방 만들기 1 ~ 4 단계 상태.
    * 입력값도 앞으로 여기에 모아서 이전 으로 돌아가도 값이 유지되게 함.
*/

class CreateRoomViewModel (
    private val roomRepository : RoomRepository = RoomRepository()
) : ViewModel() {

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

    // 개설 진행 상태 (대기 / 저장 중 / 성공 / 실패)
    private val _submitState =
        MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState : LiveData<SubmitState> =
        _submitState

    // 다음 단계로 (마지막 단계면 그대로)
    fun next() {
        val s = _step.value ?: 1
        if (s < LAST_STEP) _step.value = s + 1
    }

    // 이전 단계로 (1단계면 그대로)
    fun prev() {
        val s = _step.value ?: 1
        if (s > 1) _step.value = s - 1
    }

    // 검사에 걸린 단계로 바로 이동할 때 사용
    fun goToStep(value : Int) {
        if (value in 1..LAST_STEP) _step.value = value
    }

    // 입력 검사
    // 단계별 필수값 검사. 문제 있으면 안내 문구 , 없으면 null
    @StringRes
    fun validateStep(step : Int) : Int? = when(step) {
        1 -> if (_title.value.isNullOrBlank()) R.string.create_error_title else null
        2 -> when {
            _category.value == null -> R.string.create_error_category
            _restaurant.value.isNullOrBlank() -> R.string.create_error_restaurant
            else -> null
        }
        3 -> if (_noTimeLimit.value != true && _deadlineMinutes.value == null) R.string.create_error_deadline else null
        4 -> when {
            _minOrderAmount.value == null -> R.string.create_error_min_order
            _deliveryFee.value == null -> R.string.create_error_delivery_fee
            else -> null
        }
        else -> null
    }

    // 개설 버튼: 전체 검사 → 저장
    fun submit() {
        if (_submitState.value == SubmitState.Loading) {
            return  // 중복 클릭 방지
        }
        if (failOnInvalidStep()) return
        save(buildRoom(buildDeadline()))
    }

    // 1~4단계를 차례로 검사해서
    // 문제 있는 첫 단계로 이동하고 true, 모두 통과면 false
    private fun failOnInvalidStep(): Boolean {
        for (s in 1..LAST_STEP) {
            val error = validateStep(s) ?: continue
            fail(s, error)
            return true
        }
        return false
    }

    // 해당 단계로 이동 + 실패 상태로 안내 문구 전달
    private fun fail(step: Int, @StringRes message: Int) {
        goToStep(step)
        _submitState.value = SubmitState.Error(message)
    }

    // 마감 시간이 필요한지 ('시간 제한 없음' 체크 안 했으면 true)
    private fun needsDeadline(): Boolean =
        _noTimeLimit.value != true

    // Firestore 에 방 저장. 저장 중 → 성공(방 id) / 실패
    private fun save(room: RoomModel) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            _submitState.value = try {
                val id = roomRepository.createRoom(room)
                SubmitState.Success(id)
            } catch (e: Exception) {
                Log.w("CreateRoom", "createRoom 실패", e)
                val notLoggedIn = e is RoomException &&
                    e.reason == RoomException.Reason.NOT_LOGGED_IN
                SubmitState.Error(
                    if (notLoggedIn) R.string.create_error_not_logged_in
                    else R.string.create_error_unknown
                )
            }
        }
    }

    // 고른 시각 → Timestamp. 오늘 그 시각이 이미 지났으면 다음 날 (23:30 에 00:30 고르면 내일 00:30)
    // 시각을 안 골랐으면 null
    private fun buildDeadline(): Timestamp? {
        val minutes = _deadlineMinutes.value
            ?: return null
        val cal = todayAt(minutes)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return Timestamp(cal.time)
    }

    // 하루 중 몇 분째(18:30 → 1110)를 오늘 그 시각의 Calendar 로
    private fun todayAt(minutes: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    // 입력값을 모아 Firestore 저장용 RoomModel 로
    // (방장 id, 참여자, 상태, 생성 시각은 RoomRepository 가 채움)
    private fun buildRoom(
        deadline: Timestamp?
    ) = RoomModel(
        title = _title.value.orEmpty().trim(),
        maxParticipants = _maxParticipants.value
            ?: DEFAULT_PARTICIPANTS,
        mode = _mode.value
            ?: RoomMode.PICKUP_ONLY,
        category = _category.value
            ?: FoodCategory.ETC,
        restaurant = _restaurant.value
            .orEmpty().trim(),
        deadline = if (needsDeadline()) deadline
            else null,
        minOrderAmount = _minOrderAmount.value ?: 0,
        estimatedDeliveryFee =
            _deliveryFee.value ?: 0,
        foodCostSplitMethod = _foodCostSplit.value
            ?: FoodCostSplitMethod.PROPORTIONAL_SPLIT,
        deliveryFeeSplitMethod =
            _deliveryFeeSplit.value
                ?: DeliveryFeeSplitMethod.EQUAL_SPLIT,
    )

    // 에러 토스트를 보여준 뒤 대기 상태로 되돌림
    // (화면 회전 등으로 다시 그려질 때 토스트 중복 방지)
    fun clearSubmitError() {
        _submitState.value = SubmitState.Idle
    }

    companion object {
        const val LAST_STEP = 4
        const val TITLE_MAX = 30
        const val MIN_PARTICIPANTS = 2 // 방장 포함
        const val MAX_PARTICIPANTS = 6
        const val DEFAULT_PARTICIPANTS = 3 // 기본값 설정
        const val RESTAURANT_MAX = 30
    }

    // 개설 진행 상태 종류
    sealed interface SubmitState {
        data object Idle : SubmitState
        data object Loading : SubmitState
        data class Success (
            val roomId : String
        ) : SubmitState
        data class Error (
            @StringRes val message : Int
        ) : SubmitState
    }
}