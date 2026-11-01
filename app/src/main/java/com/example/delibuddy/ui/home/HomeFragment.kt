package com.example.delibuddy.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.delibuddy.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 카드 클릭 : 지금은 roomId 로그인 (나중에 상세 화면 이동으로 교체)
        val adapter = RoomAdapter { roomId ->
            Log.d(TAG, "방 클릭 : roomdId=$roomId")
        }
        binding.recyclerRooms.adapter = adapter

        // 방 목록이 뜰 때마다 화면 갱신
        homeViewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitList(rooms)
            binding.textEmpty.visibility = if (rooms.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}