package com.example.delibuddy.ui.chat

import com.example.delibuddy.data.chat.SharedAccount

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.delibuddy.data.order.OrderRepository
import com.example.delibuddy.data.room.RoomRepository
import com.example.delibuddy.data.room.RoomException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentChatBinding
import com.example.delibuddy.ui.chat.ChatViewModel.LeaveRoomState
import com.example.delibuddy.ui.chat.ChatViewModel.ScreenState
import com.example.delibuddy.ui.common.FullscreenImageDialogFragment
import com.example.delibuddy.ui.common.GlobalAlertDialog
import java.io.File
import java.io.IOException
import androidx.annotation.StringRes
import com.example.delibuddy.data.room.RoomModel
import com.example.delibuddy.data.room.RoomStatus
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.ui.common.profile.ProfileDialogFragment

/*
    * 채팅방
    * - 방 상세의 채팅방 버튼 / 내 채팅방 목록에서 들어옴 (roomId 필수)
    * - 글, 사진 보내기 / 사진 누르면 전체 화면으로 크게 보기
*/
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var chatAdapter: ChatAdapter
    private val participantAdapter = ChatParticipantAdapter(::showProfile)
    private var keyboardLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var wasKeyboardVisible = false
    private var cameraPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPhotoUri = savedInstanceState?.getString(STATE_CAMERA_URI)?.let(Uri::parse)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cameraPhotoUri?.let { outState.putString(STATE_CAMERA_URI, it.toString()) }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 목록, 입력창 연결과 상태 구독 시작
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMessageList()
        setUpHeader()
        setUpParticipantList()
        setUpProfileActions()
        setUpPanelTabs()
        setUpShareAccountButton()
        setUpMessageInput()
        setUpSendButton()
        setUpPhotoButton()
        setUpCameraButton()
        setUpLeaveRoomButton()
        setUpPanelButton()
        observeKeyboard()
        observeParticipants()
        observeScreenState()
        observeRoomHeader()
        observeMessages()
        setUpNotice()
        observeStatusError()
        observeLeaveRoomState()
    }

    private fun setUpLeaveRoomButton() {
        childFragmentManager.setFragmentResultListener(REQUEST_LEAVE_ROOM, viewLifecycleOwner) { _, _ ->
            viewModel.leaveRoom()
        }
        binding.chatPanel.menuLeaveRoom.setOnClickListener {
            GlobalAlertDialog.show(
                fragmentManager = childFragmentManager,
                type = GlobalAlertDialog.Type.DANGER,
                title = getString(R.string.chat_leave_room_title),
                description = getString(R.string.chat_leave_room_description),
                confirmLabel = getString(R.string.chat_menu_leave_room),
                requestKey = REQUEST_LEAVE_ROOM,
            )
        }
    }

    private fun observeLeaveRoomState() {
        viewModel.leaveRoomState.observe(viewLifecycleOwner) { state ->
            binding.chatPanel.menuLeaveRoom.isEnabled = state == LeaveRoomState.Idle
            when (state) {
                LeaveRoomState.Done -> findNavController().navigateUp()
                is LeaveRoomState.Failed -> {
                    val message = state.reason?.message ?: getString(R.string.chat_leave_room_error)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    viewModel.clearLeaveRoomFailure()
                }
                else -> Unit
            }
        }
    }

    private fun setUpHeader() {
        binding.chatHeader.buttonBack.setOnClickListener { findNavController().navigateUp() }
    }

    // 메시지 목록 : 아래부터 쌓이게
    private fun setUpMessageList() {
        chatAdapter = ChatAdapter(
            onRetryClick = { id -> viewModel.retry(id) },
            onImageClick = { url -> FullscreenImageDialogFragment.show(childFragmentManager, url) },
            onProfileClick = ::showProfile,
        )
        binding.recyclerMessages.layoutManager =
            LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        binding.recyclerMessages.adapter = chatAdapter
    }

    private fun showProfile(userId: String) {
        val mode = viewModel.roomHeader.value?.mode ?: return
        if (childFragmentManager.isStateSaved ||
            childFragmentManager.findFragmentByTag(ProfileDialogFragment.TAG) != null
        ) return
        val type = when (mode) {
            RoomMode.PICKUP_ONLY -> ProfileType.PICKUP_ONLY
            RoomMode.MEAL_BUDDY -> ProfileType.MEAL_BUDDY
        }
        val hostActions = canManageParticipant(userId)
        ProfileDialogFragment.show(
            childFragmentManager, userId, type,
            hostActions = hostActions,
            canRemove = hostActions && canRemoveParticipant(),
        )
    }

    private fun canManageParticipant(userId: String): Boolean {
        val room = viewModel.roomHeader.value ?: return false
        return viewModel.isHost(room) && room.hostId != userId && room.isParticipant(userId)
    }

    private fun canRemoveParticipant(): Boolean =
        when (viewModel.roomHeader.value?.status) {
            RoomStatus.RECRUITING, RoomStatus.COMPLETED -> true
            else -> false
        }

    private fun setUpProfileActions() {
        val userKey = ProfileDialogFragment.RESULT_USER_ID
        childFragmentManager.setFragmentResultListener(
            ProfileDialogFragment.REQUEST_VIEW_ORDER, viewLifecycleOwner,
        ) { _, result ->
            result.getString(userKey)?.let(::showParticipantOrder)
        }
        childFragmentManager.setFragmentResultListener(
            ProfileDialogFragment.REQUEST_REMOVE_PARTICIPANT, viewLifecycleOwner,
        ) { _, result ->
            val userId = result.getString(userKey) ?: return@setFragmentResultListener
            if (!canManageParticipant(userId) || !canRemoveParticipant()) return@setFragmentResultListener
            GlobalAlertDialog.show(
                fragmentManager = childFragmentManager,
                type = GlobalAlertDialog.Type.DANGER,
                title = getString(R.string.profile_remove_participant_title),
                confirmLabel = getString(R.string.profile_remove_participant),
                requestKey = REQUEST_CONFIRM_REMOVE,
                result = bundleOf(userKey to userId),
            )
        }
        childFragmentManager.setFragmentResultListener(REQUEST_CONFIRM_REMOVE, viewLifecycleOwner) { _, result ->
            val userId = result.getString(userKey) ?: return@setFragmentResultListener
            val roomId = viewModel.roomId ?: return@setFragmentResultListener
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    RoomRepository().removeParticipant(roomId, userId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val message = (e as? RoomException)?.reason?.message
                        ?: getString(R.string.profile_remove_participant_error)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showParticipantOrder(userId: String) {
        val roomId = viewModel.roomId ?: return
        if (!canManageParticipant(userId)) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val order = OrderRepository().getOrder(roomId, userId)
                if (!canManageParticipant(userId) || childFragmentManager.isStateSaved) return@launch
                val message = if (order == null) {
                    getString(R.string.profile_order_empty)
                } else {
                    getString(
                        R.string.profile_order_summary, order.price,
                        order.memo.orEmpty().ifBlank { getString(R.string.profile_order_no_memo) },
                    )
                }
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.profile_view_order)
                    .setMessage(message)
                    .setPositiveButton(R.string.profile_dialog_close, null)
                order?.cartScreenshotUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    dialog.setNeutralButton(R.string.profile_order_photo) { _, _ ->
                        FullscreenImageDialogFragment.show(childFragmentManager, url)
                    }
                }
                dialog.show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.profile_order_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpParticipantList() {
        binding.chatPanel.panelParticipants.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.chatPanel.panelParticipants.adapter = participantAdapter
        showPanel(false)
    }

    private fun setUpShareAccountButton() {
        childFragmentManager.setFragmentResultListener(
            REQUEST_SHARE_ACCOUNT, viewLifecycleOwner,
        ) { _, result ->
            val bank = result.getString(RESULT_BANK_NAME) ?: return@setFragmentResultListener
            val number = result.getString(RESULT_ACCOUNT_NUMBER) ?: return@setFragmentResultListener
            viewModel.sendAccount(SharedAccount(bank, number))
        }
        val button = binding.chatPanel.menuShareAccount
        button.setOnClickListener {
            button.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val account = viewModel.getMySharedAccount()
                    if (account == null) {
                        Toast.makeText(requireContext(), R.string.chat_share_account_missing, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    GlobalAlertDialog.show(
                        fragmentManager = childFragmentManager,
                        type = GlobalAlertDialog.Type.CONFIRM,
                        title = getString(R.string.chat_share_account_title),
                        description = getString(R.string.chat_share_account_preview, account.bankName, account.number),
                        confirmLabel = getString(R.string.chat_menu_share_account),
                        requestKey = REQUEST_SHARE_ACCOUNT,
                        result = bundleOf(RESULT_BANK_NAME to account.bankName, RESULT_ACCOUNT_NUMBER to account.number),
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.chat_share_account_error, Toast.LENGTH_SHORT).show()
                } finally {
                    button.isEnabled = true
                }
            }
        }
    }

    private fun setUpPanelTabs() {
        binding.chatPanel.tabMenu.setOnClickListener { showPanel(false) }
        binding.chatPanel.tabParticipants.setOnClickListener { showPanel(true) }
    }

    private fun setUpMessageInput() {
        binding.buttonSend.isEnabled = false
        binding.editMessage.doAfterTextChanged { text ->
            binding.buttonSend.isEnabled = !text.isNullOrBlank()
        }
        binding.editMessage.setOnClickListener { binding.chatPanel.root.isVisible = false }
        binding.editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setUpSendButton() {
        binding.buttonSend.setOnClickListener { sendMessage() }
    }

    private fun setUpPhotoButton() {
        binding.chatPanel.menuPhoto.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setUpCameraButton() {
        binding.chatPanel.menuCamera.setOnClickListener {
            try {
                val directory = File(requireContext().cacheDir, "chat_camera")
                if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create camera cache")
                val file = File.createTempFile("photo_", ".jpg", directory)
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                cameraPhotoUri = uri
                try {
                    takePhoto.launch(uri)
                } catch (e: ActivityNotFoundException) {
                    cameraPhotoUri = null
                    file.delete()
                    Toast.makeText(
                        requireContext(),
                        R.string.chat_camera_unavailable,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), R.string.chat_camera_error, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setUpPanelButton() {
        binding.buttonImage.setOnClickListener {
            val opening = !binding.chatPanel.root.isVisible
            if (opening) {
                WindowInsetsControllerCompat(requireActivity().window, binding.editMessage)
                    .hide(WindowInsetsCompat.Type.ime())
                binding.editMessage.clearFocus()
            }
            binding.chatPanel.root.isVisible = opening
        }
    }

    private fun observeParticipants() {
        viewModel.participants.observe(viewLifecycleOwner) { participantAdapter.submitList(it) }
    }

    private fun observeKeyboard() {
        val decorView = requireActivity().window.decorView
        wasKeyboardVisible = ViewCompat.getRootWindowInsets(decorView)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(decorView)
            val visible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (visible && !wasKeyboardVisible) binding.chatPanel.root.isVisible = false
            wasKeyboardVisible = visible
        }
        keyboardLayoutListener = listener
        decorView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun showPanel(participants: Boolean) {
        binding.chatPanel.indicatorMenu.visibility =
            if (participants) View.INVISIBLE else View.VISIBLE
        binding.chatPanel.indicatorParticipants.visibility =
            if (participants) View.VISIBLE else View.INVISIBLE
        binding.chatPanel.panelMenu.isVisible = !participants
        binding.chatPanel.panelParticipants.isVisible = participants
    }

    // 보내기 성공하면 입력창 비우고 맨 아래로
    private fun sendMessage() {
        val text = binding.editMessage.text?.toString().orEmpty()
        if (viewModel.send(text)) {
            binding.editMessage.text?.clear()
            scrollToBottom()
        }
    }

    // 고른 사진 보내고 맨 아래로
    private fun sendImage(uri: Uri) {
        viewModel.sendImage(uri)
        scrollToBottom()
    }

    // 방 없음 / 실패면 안내 문구, 정상이면 목록 + 입력창
    private fun observeScreenState() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            val ready = state == ScreenState.READY
            binding.recyclerMessages.isVisible = ready
            binding.layoutInput.isVisible = ready
            binding.textMessage.isVisible = !ready
            when (state) {
                ScreenState.NO_ROOM ->
                    binding.textMessage.setText(R.string.chat_no_room)

                ScreenState.ERROR ->
                    binding.textMessage.setText(R.string.chat_load_error)

                else -> Unit
            }
        }
    }

    // 상단 방 제목
    private fun observeRoomHeader() {
        viewModel.roomHeader.observe(viewLifecycleOwner) { room ->
            binding.chatHeader.textRoomTitle.text = room?.title.orEmpty()
            binding.chatHeader.textRestaurant.text = room?.restaurant.orEmpty()
            binding.chatHeader.textRestaurant.isVisible = !room?.restaurant.isNullOrBlank()
            binding.chatHeader.textCount.text = room?.let {
                getString(
                    R.string.chat_header_participant_count,
                    it.currentCount,
                    it.maxParticipants
                )
            }.orEmpty()
            renderNotice(room)
        }
    }

    // 상태 공지 펼치기/접기, 방장 버튼
    private fun setUpNotice() = with(binding.chatNotice) {
        rowSummary.setOnClickListener {
            val open = !layoutExpanded.isVisible
            layoutExpanded.isVisible = open
            imageExpand.rotation = if (open) 180f else 0f
        }

        childFragmentManager.setFragmentResultListener(
            REQUEST_ADVANCE_STATUS, viewLifecycleOwner
        ) { _, _ -> viewModel.advanceStatus() }
        buttonAction.setOnClickListener {
            confirmAdvance()
        }
    }
    // 되돌릴 수 없어서 공용 경고창으로 한 번 더 확인
    private fun confirmAdvance() {
        val status =
            viewModel.roomHeader.value?.status ?: return
        val title = when (status) {
            RoomStatus.RECRUITING ->
                R.string.notice_confirm_title_confirm
            RoomStatus.ORDER_CONFIRMED ->
                R.string.notice_confirm_title_ordered
            RoomStatus.ORDERED ->
                R.string.notice_confirm_title_received
            RoomStatus.RECEIVED ->
                R.string.notice_confirm_title_complete
            RoomStatus.COMPLETED ->
                return
        }

        GlobalAlertDialog.show(
            fragmentManager = childFragmentManager,
            type = GlobalAlertDialog.Type.CONFIRM,
            title = getString(title),
            description = getString(
                R.string.notice_confirm_message),
            confirmLabel = binding.chatNotice
                .buttonAction.text.toString(),
            requestKey = REQUEST_ADVANCE_STATUS,
        )
    }

    // 방 상태에 맞게 문구, 단계, 버튼 표시
    private fun renderNotice(room: RoomModel?) {
        binding.chatNotice.root.isVisible = room != null
        if (room == null) return
        val status = room.status
        with(binding.chatNotice) {
            textBadge.setText(noticeBadge(status))
            textSummary.text = noticeSummary(room)
            textHint.setText(noticeHint(status))
            val action = noticeAction(status)
            buttonAction.isVisible =
                action != null && viewModel.isHost(room)
            action?.let { buttonAction.setText(it) }
        }
        renderSteps(status)
    }

    // 지난 단계는 체크, 지금 단계 노랑, 남은 단계 회색
    private fun renderSteps(status: RoomStatus) =
        with(binding.chatNotice) {
            val now =
                if (status == RoomStatus.COMPLETED) 5
                else status.ordinal
            listOf(step1, step2, step3, step4, step5)
                .forEachIndexed { i, circle ->
                    circle.text =
                        if (i < now) "✓" else "${i + 1}"
                    circle.setBackgroundResource(
                        when {
                            i < now -> R.drawable.bg_step_done
                            i == now -> R.drawable.bg_step_now
                            else -> R.drawable.bg_step_todo
                        }
                    )
                }
            listOf(line1, line2, line3, line4)
                .forEachIndexed { i, line ->
                    line.setBackgroundResource(
                        if (i < now) R.color.point_orange
                        else R.color.mode_off
                    )
                }
        }

    @StringRes
    private fun noticeBadge(status: RoomStatus) =
        when (status) {
            RoomStatus.RECRUITING ->
                R.string.notice_badge_recruiting
            RoomStatus.ORDER_CONFIRMED ->
                R.string.notice_badge_confirmed
            RoomStatus.ORDERED ->
                R.string.notice_badge_ordered
            RoomStatus.RECEIVED ->
                R.string.notice_badge_received
            RoomStatus.COMPLETED ->
                R.string.notice_badge_completed
        }

    private fun noticeSummary(room: RoomModel): String {
        val now = room.currentCount
        val max = room.maxParticipants
        return when (room.status) {
            RoomStatus.RECRUITING ->
                if (room.isFull) getString(
                    R.string.notice_summary_recruiting_full,
                    now, max
                ) else getString(
                    R.string.notice_summary_recruiting,
                    now, max, max - now
                )
            RoomStatus.ORDER_CONFIRMED ->
                getString(R.string.notice_summary_confirmed)
            RoomStatus.ORDERED ->
                getString(R.string.notice_summary_ordered)
            RoomStatus.RECEIVED ->
                getString(R.string.notice_summary_received)
            RoomStatus.COMPLETED ->
                getString(R.string.notice_summary_completed)
        }
    }

    @StringRes
    private fun noticeHint(status: RoomStatus) =
        when (status) {
            RoomStatus.RECRUITING ->
                R.string.notice_hint_recruiting
            RoomStatus.ORDER_CONFIRMED ->
                R.string.notice_hint_confirmed
            RoomStatus.ORDERED ->
                R.string.notice_hint_ordered
            RoomStatus.RECEIVED ->
                R.string.notice_hint_received
            RoomStatus.COMPLETED ->
                R.string.notice_hint_completed
        }

    // 방장 버튼 문구 (완료면 버튼 숨김)
    @StringRes
    private fun noticeAction(status: RoomStatus): Int? =
        when (status) {
            RoomStatus.RECRUITING ->
                R.string.notice_action_confirm
            RoomStatus.ORDER_CONFIRMED ->
                R.string.notice_action_ordered
            RoomStatus.ORDERED ->
                R.string.notice_action_received
            RoomStatus.RECEIVED ->
                R.string.notice_action_complete
            RoomStatus.COMPLETED -> null
        }

    // 상태 변경 실패 안내
    private fun observeStatusError() {
        viewModel.statusError.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(
                requireContext(), msg, Toast.LENGTH_SHORT
            ).show()
            viewModel.clearStatusError()
        }
    }

    // 새 목록 반영. 맨 아래를 보고 있었을 때만 자동 스크롤
    private fun observeMessages() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            val wasAtBottom = isAtBottom()
            chatAdapter.submitList(items) {
                if (wasAtBottom) scrollToBottom()
            }
        }
    }

    // 더 아래로 스크롤할 수 없으면 맨 아래
    private fun isAtBottom(): Boolean =
        !binding.recyclerMessages.canScrollVertically(1)

    // 마지막 메시지로 이동
    private fun scrollToBottom() {
        val last = chatAdapter.itemCount - 1
        if (last >= 0) binding.recyclerMessages.scrollToPosition(last)
    }

    // 화면이 사라질 때 어댑터 / binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        keyboardLayoutListener?.let {
            requireActivity().window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        keyboardLayoutListener = null
        binding.recyclerMessages.adapter = null
        binding.chatPanel.panelParticipants.adapter = null
        _binding = null
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { sendImage(it) } }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = cameraPhotoUri
            cameraPhotoUri = null
            if (uri != null) {
                if (success) {
                    viewModel.sendImage(uri)
                    if (_binding != null) scrollToBottom()
                } else {
                    requireContext().contentResolver.delete(uri, null, null)
                }
            }
        }

    companion object {
        private const val STATE_CAMERA_URI = "camera_photo_uri"
        private const val REQUEST_LEAVE_ROOM = "leave_room"
        private const val REQUEST_SHARE_ACCOUNT = "share_account"
        private const val RESULT_BANK_NAME = "bank_name"
        private const val RESULT_ACCOUNT_NUMBER = "account_number"
        private const val REQUEST_CONFIRM_REMOVE = "confirm_remove_participant"
        private const val REQUEST_ADVANCE_STATUS = "advance_status"

    }

}
