package com.example.delibuddy.ui.room.create

import android.content.res.AssetManager
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.chat.ChatRepository
import com.example.delibuddy.data.chat.SystemMessage
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.DeliveryApp
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomException
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/*
    * 방 만들기 1 ~ 4 단계 상태.
    * 입력값도 앞으로 여기에 모아서 이전 으로 돌아가도 값이 유지되게 함.
*/

class CreateRoomViewModel(
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {

    // 방이 만들어지면 채팅방에 첫 안내를 남길 때 사용
    private val chatRepository = ChatRepository()

    private val _step = MutableLiveData(1)
    val step: LiveData<Int> = _step

    // 1단계 : 방 정보
    private val _title = MutableLiveData("")
    val title: LiveData<String> = _title

    private val _maxParticipants = MutableLiveData(DEFAULT_PARTICIPANTS)
    val maxParticipants: LiveData<Int> = _maxParticipants

    private val _mode = MutableLiveData(RoomMode.PICKUP_ONLY)
    val mode: LiveData<RoomMode> = _mode

    // 방 제목 저장
    fun setTitle(value: String) {
        _title.value = value
    }

    // 모드 저장 (픽업만 / 밥친구)
    fun setMode(value: RoomMode) {
        _mode.value = value
    }

    // 인원 +1 (최대 인원까지)
    fun increaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n < MAX_PARTICIPANTS) _maxParticipants.value = n + 1
    }

    // 인원 -1 (최소 인원까지)
    fun decreaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n > MIN_PARTICIPANTS) _maxParticipants.value = n - 1
    }

    // 2단계 : 음식 정보
    private val _category = MutableLiveData<FoodCategory?>(null) // null = 아직 선택 안 함
    val category: LiveData<FoodCategory?> = _category

    private val _restaurant = MutableLiveData("")
    val restaurant: LiveData<String> = _restaurant

    // 카테고리 저장 (null = 선택 안 함)
    fun setCategory(value: FoodCategory?) {
        _category.value = value
    }

    // 음식점 저장
    fun setRestaurant(value: String) {
        _restaurant.value = value
    }

    // 3단계 : 장소, 배달 앱
    private val placeRepository = RecommendLocationRepository()

    // 장소 선택 시트에 보여줄 추천 장소 (경민님 recommend-locations.json)
    private val _places = MutableLiveData<List<RecommendLocationModel>>(emptyList())
    val places: LiveData<List<RecommendLocationModel>> = _places

    private val _place = MutableLiveData<Place?>(null) // null = 아직 선택 안 함
    val place: LiveData<Place?> = _place

    // 추천 장소 목록 불러오기 (한 번만). 실패하면 직접 입력만 가능
    fun loadPlaces(assets: AssetManager) {
        if (_places.value.orEmpty().isNotEmpty()) return
        viewModelScope.launch {
            try {
                _places.value = placeRepository.getPlaces(assets)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "추천 장소 불러오기 실패", e)
            }
        }
    }

    // 추천 장소 선택 (이름 + 좌표)
    fun selectPlace(location: RecommendLocationModel) {
        _place.value = Place(location.name, location.lat, location.lon)
    }

    // 직접 입력한 장소 (좌표 없음)
    fun setCustomPlace(name: String) {
        _place.value = Place(name.trim())
    }

    private val _deliveryApp = MutableLiveData<DeliveryApp?>(null) // null = 아직 선택 안 함
    val deliveryApp: LiveData<DeliveryApp?> = _deliveryApp

    private val _deliveryAppName = MutableLiveData("") // 직접 입력한 앱 이름

    // 배달 앱 저장
    fun setDeliveryApp(value: DeliveryApp) {
        _deliveryApp.value = value
    }

    // 직접 입력한 앱 이름 저장
    fun setDeliveryAppName(value: String) {
        _deliveryAppName.value = value
    }

    // 4단계 : 금액, 정산
    private val _minOrderAmount = MutableLiveData<Int?>(null) // null = 입력 안 함
    val minOrderAmount: LiveData<Int?> = _minOrderAmount

    private val _deliveryFee = MutableLiveData<Int?>(null)
    val deliveryFee: LiveData<Int?> = _deliveryFee

    private val _foodCostSplit = MutableLiveData(FoodCostSplitMethod.PROPORTIONAL_SPLIT) // 개인 메뉴
    val foodCostSplit: LiveData<FoodCostSplitMethod> = _foodCostSplit

    private val _deliveryFeeSplit = MutableLiveData(DeliveryFeeSplitMethod.EQUAL_SPLIT) // 1/N
    val deliveryFeeSplit: LiveData<DeliveryFeeSplitMethod> = _deliveryFeeSplit

    // 최소 주문 금액 저장 (null = 입력 안 함)
    fun setMinOrderAmount(value: Int?) {
        _minOrderAmount.value = value
    }

    // 예상 배달비 저장 (null = 입력 안 함)
    fun setDeliveryFee(value: Int?) {
        _deliveryFee.value = value
    }

    // 음식값 정산 방법 저장
    fun setFoodCostSplit(value: FoodCostSplitMethod) {
        _foodCostSplit.value = value
    }

    // 배달비 정산 방법 저장
    fun setDeliveryFeeSplit(value: DeliveryFeeSplitMethod) {
        _deliveryFeeSplit.value = value
    }

    // 개설 진행 상태 (대기 / 저장 중 / 성공 / 실패)
    private val _submitState =
        MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> =
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
    fun goToStep(value: Int) {
        if (value in 1..LAST_STEP) _step.value = value
    }

    // 입력 검사
    // 단계별 필수값 검사. 문제 있으면 안내 문구 , 없으면 null
    @StringRes
    fun validateStep(step: Int): Int? = when (step) {
        1 -> if (_title.value.isNullOrBlank()) R.string.create_error_title else null
        2 -> when {
            _category.value == null -> R.string.create_error_category
            _restaurant.value.isNullOrBlank() -> R.string.create_error_restaurant
            else -> null
        }

        3 -> when {
            _place.value == null -> R.string.create_error_place
            _deliveryApp.value == null -> R.string.create_error_delivery_app
            _deliveryApp.value == DeliveryApp.OTHER &&
                    _deliveryAppName.value.isNullOrBlank() -> R.string.create_error_delivery_app_name

            else -> null
        }

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
        if (failOnInvalidStep()) {
            return
        }
        save(buildRoom())
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

    // Firestore 에 방 저장. 저장 중 → 성공(방 id) / 실패
    private fun save(room: RoomModel) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            _submitState.value = try {
                val id = roomRepository.createRoom(room)
                notifyRoomCreated(id)
                SubmitState.Success(id)
            } catch (e: CancellationException) {
                throw e
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

    // 새 채팅방에 첫 안내 남기기. 실패해도 방 만들기는 성공
    private suspend fun notifyRoomCreated(roomId: String) {
        try {
            chatRepository.sendSystem(roomId, SystemMessage.ROOM_CREATED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "시스템 메시지 실패 : $roomId", e)
        }
    }

    // 입력값을 모아 Firestore 저장용 RoomModel 로
    // (방장 id, 참여자, 상태, 생성 시각은 RoomRepository 가 채움)
    private fun buildRoom(
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
        location = _place.value?.geoPoint,
        locationName = _place.value?.name.orEmpty(),
        deliveryApp = _deliveryApp.value ?: DeliveryApp.OTHER,
        // 직접 입력일 때만 이름 저장 (아이콘 앱이면 빈 값)
        deliveryAppName = if (_deliveryApp.value == DeliveryApp.OTHER)
            _deliveryAppName.value.orEmpty().trim() else "",
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
        private const val TAG = "CreateRoomVM"
    }

    // 고른 수령 장소. 직접 입력이면 좌표 없음
    data class Place(
        val name: String,
        val lat: Double? = null,
        val lon: Double? = null,
    ) {
        val geoPoint: GeoPoint?
            get() = if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }

    // 개설 진행 상태 종류
    sealed interface SubmitState {
        data object Idle : SubmitState
        data object Loading : SubmitState
        data class Success(
            val roomId: String
        ) : SubmitState

        data class Error(
            @StringRes val message: Int
        ) : SubmitState
    }
}
