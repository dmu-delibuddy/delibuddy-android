package com.example.delibuddy.ui.chat.order

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delibuddy.R
import com.example.delibuddy.data.image.ImageRepository
import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.data.order.OrderRepository
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.data.room.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/*
    * 채팅방 > 메뉴 > 내 주문 : 내 주문 확인 / 변경
    * 모집 중(RECRUITING)일 때만 변경 가능 (OrderRepository, firestore.rules 와 같은 조건)
*/
class MyOrderViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomId: String = savedStateHandle[ARG_ROOM_ID] ?: ""
    private val orderRepository = OrderRepository()
    private val roomRepository = RoomRepository()
    private val imageRepository = ImageRepository()

    // 불러오기 상태
    sealed interface LoadState {
        data object Loading : LoadState
        data class Loaded(val order: OrderModel?, val editable: Boolean) : LoadState
        data object Error : LoadState
    }

    // 저장 상태
    sealed interface SaveState {
        data object Idle : SaveState
        data object Saving : SaveState
        data object Saved : SaveState
        data class Failed(@StringRes val message: Int) : SaveState
    }

    private val _loadState = MutableLiveData<LoadState>(LoadState.Loading)
    val loadState: LiveData<LoadState> = _loadState

    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState

    // 새로 고른 장바구니 사진 (화면 회전에도 유지)
    private val _newPhoto = MutableLiveData<Uri?>(null)
    val newPhoto: LiveData<Uri?> = _newPhoto

    // 내 주문과 방 상태 불러오기 (한 번만)
    fun load() {
        if (_loadState.value is LoadState.Loaded) return
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            _loadState.value = try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("로그인이 필요합니다.")
                val room = roomRepository.getRoom(roomId)
                val order = orderRepository.getOrder(roomId, uid)
                LoadState.Loaded(order, room?.status == RoomStatus.RECRUITING)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "내 주문 불러오기 실패 : $roomId", e)
                LoadState.Error
            }
        }
    }

    fun selectPhoto(uri: Uri) {
        _newPhoto.value = uri
    }

    // 입력 검사 → (새 사진이면 업로드) → 저장
    // 방 상세에서도 같은 화면 상태로 여러 번 저장할 수 있게, 성공하면 불러온 주문을 갱신
    fun save(price: Long?, memo: String, resolver: ContentResolver) {
        if (_saveState.value == SaveState.Saving) return // 중복 클릭 방지
        val loaded = _loadState.value as? LoadState.Loaded
        val current = loaded?.order
        val photo = _newPhoto.value
        val error = when {
            price == null || price <= 0 -> R.string.my_order_price_required
            photo == null && current?.cartScreenshotUrl.isNullOrBlank() ->
                R.string.my_order_photo_required
            else -> null
        }
        if (error != null) {
            _saveState.value = SaveState.Failed(error)
            return
        }

        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                val saved = saveOrder(
                    OrderModel(
                        price = price ?: 0L,
                        cartScreenshotUrl = current?.cartScreenshotUrl,
                        memo = memo.trim().ifEmpty { null },
                    ),
                    photo,
                    resolver,
                )
                _loadState.value = LoadState.Loaded(saved, loaded?.editable ?: true)
                _newPhoto.value = null
                SaveState.Saved
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalStateException) {
                // 모집 마감 / 참여자 아님 (OrderRepository 의 check)
                Log.w(TAG, "주문 변경 불가 : $roomId", e)
                SaveState.Failed(R.string.my_order_locked)
            } catch (e: Exception) {
                Log.e(TAG, "주문 저장 실패 : $roomId", e)
                SaveState.Failed(R.string.my_order_save_error)
            }
        }
    }

    // 새 사진이 있으면 올리고 저장. 저장에 실패하면 올린 사진은 지움. 저장한 주문을 돌려줌
    private suspend fun saveOrder(
        order: OrderModel,
        photo: Uri?,
        resolver: ContentResolver,
    ): OrderModel {
        if (photo == null) {
            orderRepository.saveMyOrder(roomId, order)
            return order
        }
        val image = imageRepository.uploadImage(photo, resolver)
        val withPhoto = order.copy(cartScreenshotUrl = image.url)
        try {
            orderRepository.saveMyOrder(roomId, withPhoto)
            return withPhoto
        } catch (e: Exception) {
            try {
                imageRepository.deleteImage(image.id)
            } catch (cleanupError: CancellationException) {
                throw cleanupError
            } catch (cleanupError: Exception) {
                e.addSuppressed(cleanupError)
            }
            throw e
        }
    }

    // 실패 안내를 보여준 뒤 대기 상태로
    fun clearSaveState() {
        _saveState.value = SaveState.Idle
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        private const val TAG = "MyOrderVM"
    }
}
