package com.example.delibuddy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
            // TODO: 방 만들기 화면 연결
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}