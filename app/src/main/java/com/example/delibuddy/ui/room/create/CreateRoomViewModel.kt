package com.example.delibuddy.ui.room.create

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.recommendation.RecommendLocationRepository
import com.example.delibuddy.data.room.DeliveryApp
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.ui.room.toRoomError
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CreateRoomViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomRepository = RoomRepository()

    private val _title = MutableLiveData("")
    val title: LiveData<String> = _title

    private val _maxParticipants = MutableLiveData(DEFAULT_PARTICIPANTS)
    val maxParticipants: LiveData<Int> = _maxParticipants

    private val _mode = MutableLiveData(RoomMode.PICKUP_ONLY)
    val mode: LiveData<RoomMode> = _mode

    fun setTitle(value: String) {
        _title.value = value
    }

    fun setMode(value: RoomMode) {
        _mode.value = value
    }

    fun increaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n < MAX_PARTICIPANTS) _maxParticipants.value = n + 1
    }

    fun decreaseParticipants() {
        val n = _maxParticipants.value ?: DEFAULT_PARTICIPANTS
        if (n > MIN_PARTICIPANTS) _maxParticipants.value = n - 1
    }

    private val _category = MutableLiveData<FoodCategory?>(null)
    val category: LiveData<FoodCategory?> = _category

    private val _restaurant = MutableLiveData("")
    val restaurant: LiveData<String> = _restaurant

    fun setCategory(value: FoodCategory?) {
        _category.value = value
    }

    fun setRestaurant(value: String) {
        _restaurant.value = value
    }

    private val placeRepository = RecommendLocationRepository()

    private val _places = MutableLiveData<List<RecommendLocationModel>>(emptyList())
    val places: LiveData<List<RecommendLocationModel>> = _places

    // 지도에서 "해당 위치로 방 만들기"로 왔으면 그 장소로 시작 (아니면 null)
    private val _place = MutableLiveData<Place?>(null)
    val place: LiveData<Place?> = _place
    init {
        loadPlaceFromMap(savedStateHandle[ARG_PLACE_ID])
    }

    // 넘겨받은 id 의 추천 장소를 Repository 에서 찾아 선택 (id 가 없거나 못 찾으면 그대로)
    private fun loadPlaceFromMap(placeId: String?) {
        if (placeId.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val location = placeRepository.getPlace(placeId)
                if (location != null && _place.value == null) selectPlace(location)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }
    }

    fun loadPlaces() {
        if (_places.value.orEmpty().isNotEmpty()) return
        viewModelScope.launch {
            try {
                _places.value = placeRepository.getPlaces()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }
    }

    fun selectPlace(location: RecommendLocationModel) {
        _place.value = Place(location.name, location.lat, location.lon)
    }

    fun setCustomPlace(name: String) {
        _place.value = Place(name.trim())
    }

    private val _deliveryApp = MutableLiveData<DeliveryApp?>(null)
    val deliveryApp: LiveData<DeliveryApp?> = _deliveryApp

    private val _deliveryAppName = MutableLiveData("")

    fun setDeliveryApp(value: DeliveryApp) {
        _deliveryApp.value = value
    }

    fun setDeliveryAppName(value: String) {
        _deliveryAppName.value = value
    }

    private val _minOrderAmount = MutableLiveData<Int?>(null)
    val minOrderAmount: LiveData<Int?> = _minOrderAmount

    private val _deliveryFee = MutableLiveData<Int?>(null)
    val deliveryFee: LiveData<Int?> = _deliveryFee

    private val _foodCostSplit = MutableLiveData(FoodCostSplitMethod.PROPORTIONAL_SPLIT)
    val foodCostSplit: LiveData<FoodCostSplitMethod> = _foodCostSplit

    private val _deliveryFeeSplit = MutableLiveData(DeliveryFeeSplitMethod.EQUAL_SPLIT)
    val deliveryFeeSplit: LiveData<DeliveryFeeSplitMethod> = _deliveryFeeSplit

    fun setMinOrderAmount(value: Int?) {
        _minOrderAmount.value = value
    }

    fun setDeliveryFee(value: Int?) {
        _deliveryFee.value = value
    }

    fun setFoodCostSplit(value: FoodCostSplitMethod) {
        _foodCostSplit.value = value
    }

    fun setDeliveryFeeSplit(value: DeliveryFeeSplitMethod) {
        _deliveryFeeSplit.value = value
    }

    private val _submitState =
        MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> =
        _submitState

    @StringRes
    fun validateStep(step: Int): Int? = when (step) {
        1 -> if (_title.value.isNullOrBlank()) R.string.room_create_error_title else null
        2 -> when {
            _category.value == null -> R.string.room_create_error_category
            _restaurant.value.isNullOrBlank() -> R.string.room_create_error_restaurant
            else -> null
        }

        3 -> when {
            _place.value == null -> R.string.room_create_error_place
            _deliveryApp.value == null -> R.string.room_create_error_delivery_app
            _deliveryApp.value == DeliveryApp.OTHER &&
                    _deliveryAppName.value.isNullOrBlank() -> R.string.room_create_error_delivery_app_name

            else -> null
        }

        4 -> when {
            _minOrderAmount.value == null -> R.string.room_create_error_min_order
            _deliveryFee.value == null -> R.string.room_create_error_delivery_fee
            else -> null
        }

        else -> null
    }

    fun submit() {
        if (_submitState.value == SubmitState.Loading) {
            return
        }
        if (failOnInvalidStep()) {
            return
        }
        save(buildRoom())
    }

    private fun failOnInvalidStep(): Boolean {
        for (s in 1..LAST_STEP) {
            val error = validateStep(s) ?: continue
            fail(s, error)
            return true
        }
        return false
    }

    private fun fail(step: Int, @StringRes message: Int) {
        _submitState.value = SubmitState.Error(message, step)
    }

    private fun save(room: RoomModel) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            _submitState.value = try {
                roomRepository.createRoom(room)
                SubmitState.Success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SubmitState.Error(e.toRoomError(R.string.room_create_error_unknown))
            }
        }
    }

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

    fun clearSubmitError() {
        _submitState.value = SubmitState.Idle
    }

    companion object {
        const val LAST_STEP = 4
        const val TITLE_MAX = 30
        const val MIN_PARTICIPANTS = 2
        const val MAX_PARTICIPANTS = 6
        const val DEFAULT_PARTICIPANTS = 3
        const val RESTAURANT_MAX = 30

        const val ARG_PLACE_ID = "placeId"
    }

    data class Place(
        val name: String,
        val lat: Double? = null,
        val lon: Double? = null,
    ) {
        val geoPoint: GeoPoint?
            get() = if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }

    sealed interface SubmitState {
        data object Idle : SubmitState
        data object Loading : SubmitState
        data object Success : SubmitState

        data class Error(
            @StringRes val message: Int,
            val step: Int? = null,
        ) : SubmitState
    }
}
