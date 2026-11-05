package com.example.delibuddy.ui.room.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.ActionState
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.RoomAction
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.databinding.FragmentRoomDetailBinding
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel.DetailState
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class RoomDetailFragment : Fragment() {

    private var _binding : FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel : RoomDetailViewModel by viewModels()

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

    // 마감, 완료는 되돌릴 수 없어서 확인 후 실행
    private fun onActionClick() {
        val room = currentRoom() ?: return
        val action = viewModel.actionFor(room)
            ?: return
        when (action) {
            RoomAction.CLOSE ->
                confirm(R.string.detail_confirm_close, action)
            RoomAction.COMPLETE ->
                confirm(R.string.detail_confirm_complete, action)
            else -> viewModel.runAction(action)
        }
    }

    // 지금 화면에 보이는 방 (로드 전이면 null)
    private fun currentRoom() : RoomModel? =
        (viewModel.state.value as? DetailState.Loaded)
            ?.room

    // 확인 창 : 확인 누르면 실행
    private fun confirm(@StringRes message : Int, action: RoomAction) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) {_, _ ->
                viewModel.runAction(action)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // 역할 / 상태에 맞게 하단 버튼 글자, 보임, 활성 여부 설정
    private fun renderActionButton(room : RoomModel) {
        val action = viewModel.actionFor(room)
        binding.buttonAction.isVisible = action != null
        if (action == null) return

        val canJoin = action != RoomAction.JOIN ||
                room.isJoinable
        val busy = viewModel.actionState.value ==
                ActionState.Loading
        binding.buttonAction.isEnabled = canJoin && !busy
        binding.buttonAction.setText (
            when {
                busy -> R.string.detail_action_loading
                canJoin -> actionLabel(action)
                else -> R.string.detail_action_unavailable
            }
        )
    }

    // 동작별 하단 버튼 글자
    @StringRes
    private fun actionLabel(action : RoomAction) =
        when (action) {
            RoomAction.JOIN -> R.string.detail_action_join
            RoomAction.LEAVE -> R.string.detail_action_leave
            RoomAction.CLOSE -> R.string.detail_action_close
            RoomAction.COMPLETE -> R.string.detail_action_complete
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
            is ActionState.Done -> {
                toast(getString(doneMessage(state.action)))
                viewModel.clearActionState()
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

    // 동작별 성공 안내 문구
    @StringRes
    private fun doneMessage(action : RoomAction) =
        when (action) {
            RoomAction.JOIN -> R.string.detail_done_join
            RoomAction.LEAVE -> R.string.detail_done_leave
            RoomAction.CLOSE -> R.string.detail_done_close
            RoomAction.COMPLETE -> R.string.detail_done_complete
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

        textRestaurant.text = row(R.string.detail_restaurant,
            room.restaurant)

        textDeadline.text = row(R.string.detail_deadline_label,
            room.deadline
                ?.let { TIME_FORMAT.format(it.toDate()) }
                ?: getString(R.string.detail_no_deadline))

        textMinOrder.text = row(R.string.detail_min_order,
            getString(R.string.detail_won, room.minOrderAmount))

        textDeliveryFee.text = row(R.string.detail_delivery_fee,
            getString(R.string.detail_won, room.estimatedDeliveryFee) +
                    " (" + getString(R.string.detail_fee_per_person,
                room.deliveryFeePerPerson) + ")")

        textFoodSplit.text = row(R.string.detail_food_split,
            getString(foodSplitLabel(room.foodCostSplitMethod)))

        textDeliverySplit.text = row(R.string.detail_delivery_split,
            getString(deliverySplitLabel(room.deliveryFeeSplitMethod)))

        renderParticipants(room)
        renderActionButton(room)
    }

    // 라벨 값 한 줄
    private fun row(@StringRes label : Int, value : String) =
        "${getString(label)} $value"

    // 방장을 맨 위에, 나머지는 참여 순서대로. 닉네임 필드 생기면 교체 예정
    private fun renderParticipants(room : RoomModel) {
        binding.textParticipantsTitle.text = getString(
            R.string.detail_participants,
            room.currentCount, room.maxParticipants)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val ordered = listOf(room.hostId) +
                room.participantIds.filter { it != room.hostId }

        binding.layoutParticipants.removeAllViews()
        ordered.forEachIndexed { index, uid ->
            val name =
                if (uid == room.hostId) getString(R.string.detail_host)
                else getString(R.string.detail_participant, index + 1)
            val me =
                if (uid == myUid) " " + getString(R.string.detail_me)
                else ""
            binding.layoutParticipants.addView (
                TextView(requireContext()).apply {
                    text = "$name$me"
                    textSize = 15f
                    setTextColor(Color.BLACK)
                    setPadding(0, 12, 0, 12)
                }
            )
        }
    }

    // 방 상태 → 화면 표시 문구 (모집 중 / 정원 마감 / 모집 마감 / 완료)
    @StringRes
    private fun statusLabel(status : RoomStatus) = when (status) {
        RoomStatus.RECRUITING -> R.string.detail_status_recruiting
        RoomStatus.FULL -> R.string.detail_status_full
        RoomStatus.ORDER_CONFIRMED -> R.string.detail_status_confirmed
        RoomStatus.COMPLETED -> R.string.detail_status_completed
    }

    // 음식값 정산 방법 → 화면 표시 문구
    @StringRes
    private fun foodSplitLabel(m : FoodCostSplitMethod) = when (m) {
        FoodCostSplitMethod.HOST_PAYS -> R.string.split_host
        FoodCostSplitMethod.EQUAL_SPLIT -> R.string.split_equal
        FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.split_by_menu
        FoodCostSplitMethod.CUSTOM -> R.string.split_custom
    }

    // 배달비 정산 방법 → 화면 표시 문구
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