package com.example.delibuddy.ui.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.databinding.FragmentRoomListBinding
import com.example.delibuddy.ui.room.detail.RoomDetailViewModel

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

    private val viewModel: RoomListViewModel by viewModels()
    private lateinit var roomAdapter: RoomAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRoomList()
        observeRooms()

        setUpModeFilter()
        setUpCategoryFilter()
        setUpJoinableOnlySwitch()
        setUpSearchInput()
    }

    // 카드 클릭 : 방 상세로 이동
    private fun setUpRoomList() {
        roomAdapter = RoomAdapter { roomId ->
            findNavController().navigate (
                R.id.action_home_to_room_detail,
                bundleOf(RoomDetailViewModel.ARG_ROOM_ID to roomId)
            )
        }
        binding.recyclerRooms.adapter = roomAdapter
    }

    private fun observeRooms() {
        viewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            roomAdapter.submitList(rooms)
            binding.textEmpty.visibility = if (rooms.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setUpModeFilter() {
        binding.groupMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setMode (
                if (checkedId == R.id.radio_meal_buddy) RoomMode.MEAL_BUDDY else RoomMode.PICKUP_ONLY
            )
        }
    }

    private fun setUpCategoryFilter() {
        binding.groupCategory.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setCategory (
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
    }

    private fun setUpJoinableOnlySwitch() {
        binding.switchJoinableOnly.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setJoinableOnly(isChecked)
        }
    }

    private fun setUpSearchInput() {
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