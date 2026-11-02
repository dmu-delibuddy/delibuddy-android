package com.example.delibuddy.ui.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.delibuddy.R
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.databinding.FragmentRoomListBinding

/*
    * 방 목록 + 필터 (모드 / 카테고리 / 참여 가능 / 검색)
    * HomeFragment 안에 FragmentContainerView 로 들어감
*/
class RoomListFragment : Fragment() {

    private var _binding: FragmentRoomListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[RoomListViewModel::class.java]

        // 카드 클릭 : 지금은 roomId 로그만 (나중에 상세 화면 이동으로 교체)
        val adapter = RoomAdapter { roomId ->
            Log.d(TAG, "방 클릭 : roomId=$roomId")
        }
        binding.recyclerRooms.adapter = adapter

        // 방 목록이 올 때마다 화면 갱신
        viewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            adapter.submitList(rooms)
            binding.textEmpty.visibility = if (rooms.isEmpty()) View.VISIBLE else View.GONE
        }

        // ----------- 필터 --------------
        // 모드 : 픽업 | 밥 친구
        binding.groupMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setMode(
                if (checkedId == R.id.radio_meal_buddy) RoomMode.MEAL_BUDDY else RoomMode.PICKUP_ONLY
            )
        }

        // 카테고리 : 전체면 null
        binding.groupCategory.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setCategory(
                when (checkedId) {
                    R.id.radio_cat_kor -> FoodCategory.KOR
                    R.id.radio_cat_chn -> FoodCategory.CHN
                    R.id.radio_cat_jpn -> FoodCategory.JPN
                    R.id.radio_cat_west -> FoodCategory.WEST
                    R.id.radio_cat_etc -> FoodCategory.ETC
                    else -> null
                }
            )
        }

        // 참여 가능한 방만 보기
        binding.switchJoinableOnly.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setJoinableOnly(isChecked)
        }

        // 검색 : 글자를 입력할 때마다
        binding.editSearch.doAfterTextChanged { text ->
            viewModel.setQuery(text?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerRooms.adapter = null   // 어댑터가 사라진 화면을 붙잡지 않게
        _binding = null
    }

    companion object {
        private const val TAG = "RoomListFragment"
    }
}