package com.example.delibuddy.ui.chat.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.data.order.OrderRepository
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderSheetViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomRepository = RoomRepository()
    private val orderRepository = OrderRepository()
    private val profileRepository = ProfileRepository()

    private val roomId: String = savedStateHandle[ARG_ROOM_ID] ?: ""

    private val nicknames = MutableStateFlow<Map<String, String>>(emptyMap())
    private val requestedIds = mutableSetOf<String>()

    val sheet: LiveData<OrderSheet?> =
        combine(
            roomRepository.observeRoom(roomId),
            orderRepository.observeOrders(roomId),
            nicknames,
        ) { room, orders, names ->
            room?.let {
                requestNicknames(it)
                buildSheet(it, orders, names)
            }
        }.catch { e ->
            Log.w(TAG, "주문확인서 불러오기 실패 : $roomId", e)
            emit(null)
        }.asLiveData()

    private fun buildSheet(
        room: RoomModel,
        orders: List<OrderModel>,
        names: Map<String, String>,
    ): OrderSheet {
        val ids = room.participantIds.filter { it.isNotBlank() }.distinct()
        val ordersById = orders.associateBy { it.userId }
        val people = ids.size.coerceAtLeast(1)
        val foodTotal = ids.sumOf { ordersById[it]?.price ?: 0L }
        val fee = room.estimatedDeliveryFee.toLong()

        val rows = ids.map { uid ->
            val order = ordersById[uid]
            val isHost = room.isHost(uid)
            OrderSheetRow(
                userId = uid,
                nickname = names[uid].orEmpty(),
                isHost = isHost,
                order = order,
                foodShare = foodShare(room.foodCostSplitMethod, order, foodTotal, people, isHost),
                deliveryShare = deliveryShare(room.deliveryFeeSplitMethod, fee, people, isHost),
            )
        }.sortedByDescending { it.isHost }

        return OrderSheet(room, rows, foodTotal, fee)
    }

    private fun foodShare(
        method: FoodCostSplitMethod,
        order: OrderModel?,
        total: Long,
        people: Int,
        isHost: Boolean,
    ): Long? = when (method) {
        FoodCostSplitMethod.PROPORTIONAL_SPLIT -> order?.price ?: 0L
        FoodCostSplitMethod.EQUAL_SPLIT -> total / people + if (isHost) total % people else 0L
        FoodCostSplitMethod.HOST_PAYS -> if (isHost) total else 0L
        FoodCostSplitMethod.CUSTOM -> null
    }

    private fun deliveryShare(
        method: DeliveryFeeSplitMethod,
        fee: Long,
        people: Int,
        isHost: Boolean,
    ): Long? = when (method) {
        DeliveryFeeSplitMethod.EQUAL_SPLIT -> fee / people + if (isHost) fee % people else 0L
        DeliveryFeeSplitMethod.HOST_PAYS -> if (isHost) fee else 0L
        DeliveryFeeSplitMethod.CUSTOM -> null
    }

    private fun requestNicknames(room: RoomModel) {
        val type = when (room.mode) {
            RoomMode.PICKUP_ONLY -> ProfileType.PICKUP_ONLY
            RoomMode.MEAL_BUDDY -> ProfileType.MEAL_BUDDY
        }
        room.participantIds
            .filter { it.isNotBlank() && requestedIds.add(it) }
            .forEach { uid ->
                viewModelScope.launch {
                    try {
                        val name = profileRepository.getProfile(uid, type)?.nickname.orEmpty()
                        nicknames.update { it + (uid to name) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "닉네임 조회 실패 : $uid", e)
                    }
                }
            }
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        private const val TAG = "OrderSheetVM"
    }
}
