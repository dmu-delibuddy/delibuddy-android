package com.example.delibuddy.ui.room.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.ActionState
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.databinding.FragmentRoomDetailBinding
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.DetailState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

import android.net.Uri
import android.widget.ImageView
import android.graphics.drawable.GradientDrawable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.delibuddy.data.order.OrderRepository
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class RoomDetailFragment : Fragment() {

    private var _binding : FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel : RoomDetailViewModel by viewModels()

    private var selectedImage: Uri? = null
    private var screenshotUrl: String? = null
    private var orderLoaded = false
    private val orderRepository = OrderRepository()
    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && _binding != null) {
            selectedImage = uri
            Glide.with(this).load(uri).into(binding.order.imageOrderPreview)
        }
    }

    // 화면 레이아웃(ViewBinding) 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoomDetailBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    // 버튼 연결 + 방 정보 / 처리 상태 구독 시작
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBackButton()
        setUpActionButton()
        observeRoom()
        observeActionState()
        setUpOrder()
    }

    // 상단 < 버튼 : 이전 화면(방 목록)으로
    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // dp → px 변환
    private fun Int.dp() : Int =
        (this * resources.displayMetrics.density).toInt()

    // 하단 버튼 클릭 연결
    private fun setUpActionButton() {
        binding.buttonAction.setOnClickListener {
            onActionClick()
        }
    }

    private fun onActionClick() {
        val room = currentRoom() ?: return
        if (viewModel.canChat(room)) {
            openChat()
        } else {
            viewModel.joinRoom()
        }
    }

    private fun openChat() {
        findNavController().navigate(
            R.id.action_room_detail_to_chat,
            bundleOf(RoomDetailViewModel.ARG_ROOM_ID to viewModel.roomId)
        )
    }

    // 지금 화면에 보이는 방 (로드 전이면 null)
    private fun currentRoom() : RoomModel? =
        (viewModel.state.value as? DetailState.Loaded)
            ?.room

    // 참여 여부에 따라 버튼 문구와 활성 여부를 설정한다.
    private fun renderActionButton(room : RoomModel) {
        val joined = viewModel.canChat(room)
        binding.buttonAction.isVisible = true
        binding.buttonAction.isEnabled = (joined || room.isJoinable) &&
                viewModel.actionState.value != ActionState.Loading
        binding.buttonAction.setText(if (joined) R.string.detail_go_chat else R.string.detail_action_join)
        binding.buttonAction.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), if (joined) R.color.mode_off else R.color.button_red
        )
        binding.buttonAction.setTextColor(ContextCompat.getColor(
            requireContext(), if (joined) R.color.black else R.color.white
        ))
    }

    // 처리 상태 : 버튼 갱신 + 결과 토스트
    private fun observeActionState() {
        viewModel.actionState
            .observe(viewLifecycleOwner) {state ->
                currentRoom()?.let { renderActionButton(it) }
                handleActionResult(state)
            }
    }

    // 성공, 실패 안내 후 대기 상태로
    private fun handleActionResult(state : ActionState) {
        when (state) {
            ActionState.Done -> {
                toast(getString(R.string.detail_done_join))
                viewModel.clearActionState()
                openChat()
            }
            is ActionState.Failed -> {
                toast (
                    state.reason?.message
                        ?:getString(R.string.detail_action_error)
                )
                viewModel.clearActionState()
            }
            else -> Unit
        }
    }

    // 짧은 안내 토스트 띄우기
    private fun toast(message : String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // 상태에 따라 내용 | 안내 문구 전환
    private fun observeRoom() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            val loaded = state is RoomDetailViewModel.DetailState.Loaded
            binding.progress.isVisible = false
            binding.layoutContent.isVisible = loaded
            binding.textMessage.isVisible = !loaded
            binding.buttonAction.isVisible = false // 로드 전, 실패 시
            when (state) {
                is DetailState.Loaded -> renderRoom(state.room)
                DetailState.NotFound -> binding.textMessage
                    .setText(R.string.detail_not_found)
                DetailState.Error -> binding.textMessage
                    .setText(R.string.detail_load_error)
            }
        }
    }

    // 방 정보(상태, 제목, 모드, 가게, 마감, 금액, 정산) + 참여자 + 버튼 표시
    private fun renderRoom(room : RoomModel) = with(binding) {

        textStatus.text = getString(statusLabel(room.status)) + " " +
                getString(R.string.detail_count, room.currentCount, room.maxParticipants)
        textTitle.text = room.title
        textMode.setText (
            if (room.mode == RoomMode.MEAL_BUDDY) R.string.mode_meal_buddy
            else R.string.mode_pickup
        )

        information.textRestaurant.text = row(R.string.detail_restaurant,
            room.restaurant)

        information.textDeadline.text = row(R.string.detail_deadline_label,
            room.deadline
                ?.let { TIME_FORMAT.format(it.toDate()) }
                ?: getString(R.string.detail_no_deadline))

        information.textMinOrder.text = row(R.string.detail_min_order,
            getString(R.string.detail_won, room.minOrderAmount))

        information.textDeliveryFee.text = row(R.string.detail_delivery_fee,
            getString(R.string.detail_won, room.estimatedDeliveryFee) +
                    " (" + getString(R.string.detail_fee_per_person,
                room.deliveryFeePerPerson) + ")")

        information.textFoodSplit.text = row(R.string.detail_food_split,
            getString(foodSplitLabel(room.foodCostSplitMethod)))

        information.textDeliverySplit.text = row(R.string.detail_delivery_split,
            getString(deliverySplitLabel(room.deliveryFeeSplitMethod)))

        renderParticipants(room)
        renderActionButton(room)
    }

    // 라벨 값 한 줄
    private fun row(@StringRes label : Int, value : String) =
        "${getString(label)} $value"

    // 방장을 맨 위에, 나머지는 참여 순서대로. 닉네임 필드 생기면 교체 예정
    private fun renderParticipants(room : RoomModel) {
        binding.participants.textParticipantsTitle.text = getString(
            R.string.detail_participants,
            room.currentCount, room.maxParticipants)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val ordered = listOf(room.hostId) +
                room.participantIds.filter { it != room.hostId }

        val container = binding.participants.layoutParticipants
        container.removeAllViews()
        val slots = maxOf(6, room.currentCount, room.maxParticipants)
        var row: LinearLayout? = null
        for (index in 0 until slots) {
            if (index % 3 == 0) {
                row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                container.addView(row, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8.dp() })
            }
            val cardRow = requireNotNull(row)
            val uid = ordered.getOrNull(index)
            val fallbackName = when {
                uid == room.hostId -> "방장"
                uid == myUid && uid != null -> "나"
                uid != null -> "참여자 ${index + 1}"
                index < room.maxParticipants -> "빈자리"
                else -> "모집 안 함"
            }
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(4.dp(), 10.dp(), 4.dp(), 10.dp())
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 8.dp().toFloat()
                    setStroke(1.dp(), Color.rgb(220, 224, 232))
                }
            }
            val avatar = ImageView(requireContext()).apply {
                setImageResource(R.drawable.mypage_icon)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = if (uid != null) "$fallbackName 프로필 사진" else null
                background = GradientDrawable().apply {
                    setColor(Color.rgb(245, 246, 248))
                    cornerRadius = 24.dp().toFloat()
                }
                clipToOutline = true
            }
            card.addView(avatar, LinearLayout.LayoutParams(48.dp(), 48.dp()))
            val name = TextView(requireContext()).apply {
                text = fallbackName
                textSize = 15f
                gravity = android.view.Gravity.CENTER
                setTextColor(if (uid == null) Color.GRAY else Color.BLACK)
                setPadding(0, 6.dp(), 0, 0)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            card.addView(name, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            cardRow.addView(card, LinearLayout.LayoutParams(0, 100.dp(), 1f).apply {
                if (index % 3 != 2) marginEnd = 8.dp()
            })

            if (uid != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val profile = try {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid).get().await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                    if (_binding == null || card.parent !== cardRow || cardRow.parent !== container || profile == null) return@launch
                    val nickname = profile.getString("nickname")?.takeIf { it.isNotBlank() }
                    val photoUrl = profile.getString("photoUrl")?.takeIf { it.isNotBlank() }
                    name.text = nickname ?: fallbackName
                    name.contentDescription = name.text
                    if (photoUrl != null) {
                        Glide.with(this@RoomDetailFragment)
                            .load(photoUrl)
                            .placeholder(R.drawable.mypage_icon)
                            .error(R.drawable.mypage_icon)
                            .circleCrop()
                            .into(avatar)
                    }
                }
            }
        }
        renderOrderAvailability(room)
        if (!orderLoaded && viewModel.canChat(room)) {
            orderLoaded = true
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    val order = orderRepository.getOrder(viewModel.roomId, uid) ?: return@launch
                    if (_binding == null) return@launch
                    // 복원 시 진행 중인 입력은 유지한다.
                    val price = binding.order.inputOrderPrice
                    val memo = binding.order.inputOrderMemo
                    if (price.text.isEmpty()) price.setText(order.price.toString())
                    if (memo.text.isEmpty()) memo.setText(order.memo.orEmpty())
                    screenshotUrl = order.cartScreenshotUrl
                    if (selectedImage == null && screenshotUrl != null) {
                        Glide.with(this@RoomDetailFragment).load(screenshotUrl)
                            .into(binding.order.imageOrderPreview)
                    }
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) { toast("주문 정보를 불러오지 못했습니다.") }
            }
        }
    }

    private fun setUpOrder() {
        binding.order.imageOrderPreview.setOnClickListener {
            photoPicker.launch("image/*")
        }
        binding.order.inputOrderMemo.doAfterTextChanged {
            binding.order.textMemoCount.text = "${it?.length ?: 0}/100"
        }
    }

    private fun renderOrderAvailability(room: RoomModel) {
        val editable = viewModel.canChat(room) && !room.isDeadLinePassed &&
            room.status in setOf(RoomStatus.RECRUITING, RoomStatus.FULL)
        val enabled = editable
        with(binding.order) {
            inputOrderPrice.isEnabled = enabled
            inputOrderMemo.isEnabled = enabled
            imageOrderPreview.isEnabled = enabled
        }
        binding.order.textOrderMessage.text = when {
            !viewModel.canChat(room) -> "방에 참여한 후 주문을 입력할 수 있습니다."
            !editable -> "모집이 마감되어 주문을 변경할 수 없습니다."
            else -> ""
        }
    }

    // 방 상태 => 화면 표시 문구 (모집 중 / 정원 마감 / 모집 마감 / 완료)
    @StringRes
    private fun statusLabel(status : RoomStatus) = when (status) {
        RoomStatus.RECRUITING -> R.string.detail_status_recruiting
        RoomStatus.FULL -> R.string.detail_status_full
        RoomStatus.ORDER_CONFIRMED -> R.string.detail_status_confirmed
        RoomStatus.COMPLETED -> R.string.detail_status_completed
    }

    // 음식값 정산 방법 => 화면 표시 문구
    @StringRes
    private fun foodSplitLabel(m : FoodCostSplitMethod) = when (m) {
        FoodCostSplitMethod.HOST_PAYS -> R.string.split_host
        FoodCostSplitMethod.EQUAL_SPLIT -> R.string.split_equal
        FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.split_by_menu
        FoodCostSplitMethod.CUSTOM -> R.string.split_custom
    }

    // 배달비 정산 방법 => 화면 표시 문구
    @StringRes
    private fun deliverySplitLabel(m : DeliveryFeeSplitMethod) = when (m) {
        DeliveryFeeSplitMethod.HOST_PAYS -> R.string.split_host
        DeliveryFeeSplitMethod.EQUAL_SPLIT -> R.string.split_equal
        DeliveryFeeSplitMethod.CUSTOM -> R.string.split_custom
    }

    // 화면이 사라질 때 binding 해제 (메모리 누수 방지)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // 예 : 오후 6:30
        private val TIME_FORMAT =
            SimpleDateFormat("a h:mm", Locale.KOREA)
    }
}
