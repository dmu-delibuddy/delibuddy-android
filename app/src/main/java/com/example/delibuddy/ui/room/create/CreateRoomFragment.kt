package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.delibuddy.databinding.FragmentCreateRoomBinding

// 방 만들기. 지금은 화면 이동 확인용 빈 화면
class CreateRoomFragment : Fragment() {

    private var _binding : FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}