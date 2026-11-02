package com.example.delibuddy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentHomeBinding

/*
    * 홈 화면. 방 목록·필터는 RoomListFragment 가 담당 (fragment_home.xml 의 container_room_list)
    * 홈은 방 만들기 버튼과, 나중에 붙일 섹션(내 주문 상태 등)만 가진다
*/
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCreateRoom.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_create_room)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}