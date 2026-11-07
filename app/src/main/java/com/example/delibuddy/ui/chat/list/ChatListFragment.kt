package com.example.delibuddy.ui.chat.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentChatListBinding
import com.example.delibuddy.ui.chat.ChatViewModel
import com.example.delibuddy.ui.room.list.RoomAdapter

/*
    * 내 채팅방 목록. 방 카드를 누르면 그 방 채팅으로 이동
*/
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()

    private lateinit var roomAdapter: RoomAdapter

    // 화면 레이아웃(ViewBinding) 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatListBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    // 목록 연결과 내 채팅방 구독 시작
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRoomList()
        observeRooms()
    }

    // 방 카드 클릭 : roomId 를 넘기며 채팅방으로 이동
    private fun setUpRoomList() {
        roomAdapter = RoomAdapter { roomId ->
            findNavController().navigate(
                R.id.action_chat_list_to_chat_room,
                bundleOf(ChatViewModel.ARG_ROOM_ID to roomId),
            )
        }
        binding.recyclerRooms.adapter = roomAdapter
    }

    // 목록 표시, 비어 있으면 안내 문구
    private fun observeRooms() {
        viewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            roomAdapter.submitList(rooms)
            binding.textEmpty.isVisible = rooms.isEmpty()
        }
    }

    // 화면이 사라질 때 어댑터 / binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerRooms.adapter = null
        _binding = null
    }
}