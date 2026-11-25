package com.example.delibuddy.ui.room.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
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
import com.example.delibuddy.ui.room.deliveryAppLabel
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.DetailState

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.delibuddy.data.room.RoomException
import com.example.delibuddy.ui.common.GlobalAlertDialog
import com.example.delibuddy.ui.common.GlobalLoadingDialog
import com.example.delibuddy.ui.chat.order.MyOrderViewModel
import com.example.delibuddy.ui.chat.order.fill
import com.example.delibuddy.ui.chat.order.saveTo
import com.example.delibuddy.ui.chat.order.setUpMemoCount
import com.example.delibuddy.ui.chat.order.MyOrderViewModel.LoadState
import com.example.delibuddy.ui.chat.order.MyOrderViewModel.SaveState

class RoomDetailFragment : Fragment() {

    private var _binding: FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoomDetailViewModel by viewModels()

    // 참여 후 내 주문 불러오기 / 저장 (채팅방 내 주문 창과 같은 ViewModel, 같은 roomId 인자)
    private val orderViewModel: MyOrderViewModel by viewModels()

    private var selectedImage: Uri? = null // 참여할 때 올릴 사진
    private var participantRenderer: ParticipantRenderer? = null
    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && _binding != null) {
                // 참여 전 사진은 참여할 때만 씀. 내 주문 ViewModel 에 남으면 돌아왔을 때 다시 올라감
                val joined = currentRoom()?.let(viewModel::canChat) == true
                if (joined) orderViewModel.selectPhoto(uri) else selectedImage = uri
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
        participantRenderer = ParticipantRenderer(this, binding.participants)
        setUpBackButton()
        setUpActionButton()
        observeRoom()
        observeActionState()
        setUpOrder()
        observeMyOrder()
        observeOrderSaveState()
    }

    // 상단 < 버튼 : 이전 화면(방 목록)으로
    private fun setUpBackButton() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

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
            val price = binding.order.orderPriceAmount.editText.text.toString().trim().toLongOrNull()
            val photo = selectedImage
            if (price == null || price <= 0 || photo == null) {
                GlobalAlertDialog.show(
                    fragmentManager = childFragmentManager,
                    type = GlobalAlertDialog.Type.INFO,
                    title = getString(R.string.detail_join_order_alert),
                )
                return
            }
            val memo = binding.order.inputOrderMemo.text.toString().trim().ifEmpty { null }
            viewModel.joinRoom(price, photo, memo, requireContext().contentResolver)
        }
    }

    private fun openChat() {
        findNavController().navigate(
            R.id.action_room_detail_to_chat,
            bundleOf(RoomDetailViewModel.ARG_ROOM_ID to viewModel.roomId)
        )
    }

    // 지금 화면에 보이는 방 (로드 전이면 null)
    private fun currentRoom(): RoomModel? =
        (viewModel.state.value as? DetailState.Loaded)
            ?.room

    // 참여 여부에 따라 버튼 문구와 활성 여부를 설정한다.
    private fun renderActionButton(room: RoomModel) {
        val joined = viewModel.canChat(room)
        binding.buttonAction.isVisible = true
        binding.buttonAction.isEnabled = (joined || room.isJoinable) &&
                viewModel.actionState.value != ActionState.Loading
        binding.buttonAction.setText(if (joined) R.string.detail_go_chat else R.string.detail_action_join)
        binding.buttonAction.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), if (joined) R.color.mode_off else R.color.button_red
        )
        binding.buttonAction.setTextColor(
            ContextCompat.getColor(
                requireContext(), if (joined) R.color.black else R.color.white
            )
        )
    }

    // 처리 상태 : 버튼 갱신 + 결과 토스트
    private fun observeActionState() {
        viewModel.actionState
            .observe(viewLifecycleOwner) { state ->
                currentRoom()?.let { renderActionButton(it) }
                handleActionResult(state)
            }
    }

    // 성공, 실패 안내 후 대기 상태로
    private fun handleActionResult(state: ActionState) {
        when (state) {
            ActionState.Done -> {
                toast(getString(R.string.detail_done_join))
                viewModel.clearActionState()
                openChat()
            }

            is ActionState.Failed -> {
                if (state.reason ==
                    RoomException.Reason.ACCOUNT_REQUIRED
                ) {
                    GlobalAlertDialog.show(
                        fragmentManager = childFragmentManager,
                        type = GlobalAlertDialog.Type.INFO,
                        title = getString(
                            R.string.detail_account_required_title),
                        description = state.reason.message,
                    )
                } else {
                    toast(
                        state.reason?.message
                            ?: getString(R.string.detail_action_error)
                    )
                }
                viewModel.clearActionState()
            }

            else -> Unit
        }
    }

    // 짧은 안내 토스트 띄우기
    private fun toast(message: String) {
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
    private fun renderRoom(room: RoomModel) = with(binding) {

        textStatus.text = getString(statusLabel(room)) + " " +
                getString(R.string.detail_count, room.currentCount, room.maxParticipants)
        textStatus.setBackgroundResource(statusBackground(room))
        textStatus.isActivated = room.status != RoomStatus.RECRUITING || room.isFull
        textTitle.text = room.title
        textMode.root.displayedChild = if (room.mode == RoomMode.MEAL_BUDDY) 1 else 0

        information.textRestaurant.text = row(
            R.string.detail_restaurant,
            room.restaurant
        )

        information.textPlace.text = row(
            R.string.detail_place,
            room.locationName.ifBlank { getString(R.string.detail_place_none) }
        )

        information.textDeliveryApp.text = row(
            R.string.detail_delivery_app,
            room.deliveryAppLabel(requireContext())
        )


        information.textMinOrder.text = row(
            R.string.detail_min_order,
            getString(R.string.detail_won, room.minOrderAmount)
        )

        information.textDeliveryFee.text = row(
            R.string.detail_delivery_fee,
            getString(R.string.detail_won, room.estimatedDeliveryFee) +
                    " (" + getString(
                R.string.detail_fee_per_person,
                room.deliveryFeePerPerson
            ) + ")"
        )

        information.textFoodSplit.text = row(
            R.string.detail_food_split,
            getString(foodSplitLabel(room.foodCostSplitMethod))
        )

        information.textDeliverySplit.text = row(
            R.string.detail_delivery_split,
            getString(deliverySplitLabel(room.deliveryFeeSplitMethod))
        )

        participantRenderer?.render(room)
        renderOrder(room)
        renderActionButton(room)

    }

    // 라벨 값 한 줄
    private fun row(@StringRes label: Int, value: String) =
        "${getString(label)} $value"

    // 참여한 방이면 내 주문 불러오기 (한 번만)
    private fun renderOrder(room: RoomModel) {
        renderOrderAvailability(room)
        if (viewModel.canChat(room)) orderViewModel.load()
    }

    // 사진 고르기, 메모 글자 수, (참여 후) 주문 저장
    private fun setUpOrder() = with(binding.order) {
        imageOrderPreview.setOnClickListener { photoPicker.launch("image/*") }
        setUpMemoCount()
        buttonSaveOrder.setOnClickListener {
            saveTo(orderViewModel, requireContext().contentResolver)
        }
    }

    // 불러온 내 주문 채우기 (복원 시 진행 중인 입력은 유지)
    private fun observeMyOrder() {
        orderViewModel.loadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoadState.Loaded -> state.order?.let {
                    binding.order.fill(it, orderViewModel.newPhoto.value != null, this)
                }
                is LoadState.Error -> toast(getString(state.message))
                LoadState.Loading -> Unit
            }
        }
    }

    // 주문 저장 중 로딩, 결과 안내
    private fun observeOrderSaveState() {
        orderViewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SaveState.Saving ->
                    GlobalLoadingDialog.show(childFragmentManager, getString(R.string.my_order_saving))
                SaveState.Saved -> {
                    GlobalLoadingDialog.hide(childFragmentManager)
                    toast(getString(R.string.my_order_saved))
                    orderViewModel.clearSaveState()
                }
                is SaveState.Failed -> {
                    GlobalLoadingDialog.hide(childFragmentManager)
                    toast(getString(state.message))
                    orderViewModel.clearSaveState()
                }
                SaveState.Idle -> Unit
            }
        }
    }

    private fun renderOrderAvailability(room: RoomModel) {
        val joined = viewModel.canChat(room)
        val editable = if (joined) room.status == RoomStatus.RECRUITING else room.isJoinable
        val enabled = editable
        with(binding.order) {
            orderPriceAmount.editText.isEnabled = enabled
            inputOrderMemo.isEnabled = enabled
            imageOrderPreview.isEnabled = enabled
            buttonSaveOrder.isVisible = joined && editable // 참여 후 모집 중일 때만 저장
        }
        binding.order.textOrderMessage.text = when {
            !joined && editable -> getString(R.string.detail_join_order_alert)
            !joined -> getString(R.string.detail_join_closed) // 정원 마감 / 모집 마감
            !editable -> getString(R.string.my_order_locked)
            else -> ""
        }
    }

    // 방 상태 => 표시 문구 별 배경색 변경
    @DrawableRes
    private fun statusBackground(room: RoomModel) = when(room.status) {
        RoomStatus.RECRUITING ->
            if(room.isFull) R.drawable.bg_badge_red
            else R.drawable.bg_badge_yellow
        RoomStatus.ORDER_CONFIRMED ->
            R.drawable.bg_badge_orange
        RoomStatus.ORDERED ->
            R.drawable.bg_badge_green
        RoomStatus.RECEIVED ->
            R.drawable.bg_badge_red
        RoomStatus.COMPLETED ->
            R.drawable.bg_badge_gray
    }

    // 방 상태 => 화면 표시 문구 (모집 중인데 정원이 차면 정원 마감)
    @StringRes
    private fun statusLabel(room: RoomModel) = when (room.status) {
        RoomStatus.RECRUITING ->
            if (room.isFull) R.string.detail_status_full
            else R.string.detail_status_recruiting
        RoomStatus.ORDER_CONFIRMED -> R.string.detail_status_confirmed
        RoomStatus.ORDERED -> R.string.detail_status_ordered
        RoomStatus.RECEIVED -> R.string.detail_status_received
        RoomStatus.COMPLETED -> R.string.detail_status_completed
    }

    // 음식값 정산 방법 => 화면 표시 문구
    @StringRes
    private fun foodSplitLabel(m: FoodCostSplitMethod) = when (m) {
        FoodCostSplitMethod.HOST_PAYS -> R.string.split_host
        FoodCostSplitMethod.EQUAL_SPLIT -> R.string.split_equal
        FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.split_by_menu
        FoodCostSplitMethod.CUSTOM -> R.string.split_custom
    }

    // 배달비 정산 방법 => 화면 표시 문구
    @StringRes
    private fun deliverySplitLabel(m: DeliveryFeeSplitMethod) = when (m) {
        DeliveryFeeSplitMethod.HOST_PAYS -> R.string.split_host
        DeliveryFeeSplitMethod.EQUAL_SPLIT -> R.string.split_equal
        DeliveryFeeSplitMethod.CUSTOM -> R.string.split_custom
    }

    // 화면이 사라질 때 binding 해제 (메모리 누수 방지)
    override fun onDestroyView() {
        participantRenderer = null
        super.onDestroyView()
        _binding = null
    }
}
