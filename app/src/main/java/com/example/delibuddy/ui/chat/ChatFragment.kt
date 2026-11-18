package com.example.delibuddy.ui.chat

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentChatBinding
import com.example.delibuddy.ui.chat.ChatViewModel.ScreenState
import com.example.delibuddy.ui.common.FullscreenImageDialogFragment
import java.io.File
import java.io.IOException

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
    private val participantAdapter = ChatParticipantAdapter()
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
        setUpPanelTabs()
        setUpMessageInput()
        setUpSendButton()
        setUpPhotoButton()
        setUpCameraButton()
        setUpPanelButton()
        observeKeyboard()
        observeParticipants()
        observeScreenState()
        observeRoomHeader()
        observeMessages()
    }

    private fun setUpHeader() {
        binding.chatHeader.buttonBack.setOnClickListener { findNavController().navigateUp() }
    }

    // 메시지 목록 : 아래부터 쌓이게
    private fun setUpMessageList() {
        chatAdapter = ChatAdapter(
            onRetryClick = { id -> viewModel.retry(id) },
            onImageClick = { url -> FullscreenImageDialogFragment.show(childFragmentManager, url) },
        )
        binding.recyclerMessages.layoutManager =
            LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        binding.recyclerMessages.adapter = chatAdapter
    }

    private fun setUpParticipantList() {
        binding.chatPanel.panelParticipants.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.chatPanel.panelParticipants.adapter = participantAdapter
        showPanel(false)
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
    }

}
