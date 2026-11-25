package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.databinding.FragmentCreateRoomInfoBinding

class CreateRoomInfoFragment : Fragment() {

    private var _binding: FragmentCreateRoomInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateRoomViewModel by navGraphViewModels(R.id.navigation_create_room)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateRoomInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCreateRoomHeader(binding.header, STEP)
        setUpCreateRoomBottom(binding.bottom, STEP) { goNextIfValid() }
        setUpTitleInput()
        setUpParticipantButtons()
        observeParticipants()
        setUpModeSelect()
    }

    private fun goNextIfValid() {
        val error = viewModel.validateStep(STEP)
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(R.id.action_create_room_info_to_food)
    }

    private fun setUpTitleInput() {
        binding.editTitle.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            viewModel.setTitle(value)
            binding.textTitleCount.text = "${value.length}/${CreateRoomViewModel.TITLE_MAX}"
        }
    }

    private fun setUpParticipantButtons() {
        binding.buttonMinus.setOnClickListener { viewModel.decreaseParticipants() }
        binding.buttonPlus.setOnClickListener { viewModel.increaseParticipants() }
    }

    private fun observeParticipants() {
        viewModel.maxParticipants.observe(viewLifecycleOwner) { n ->
            binding.textParticipants.text = "${n}명"
            binding.buttonMinus.isEnabled = n > CreateRoomViewModel.MIN_PARTICIPANTS
            binding.buttonPlus.isEnabled = n < CreateRoomViewModel.MAX_PARTICIPANTS
        }
    }

    private fun setUpModeSelect() {
        binding.groupCreateMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setMode(
                if (checkedId == R.id.radio_create_meal_buddy) RoomMode.MEAL_BUDDY else RoomMode.PICKUP_ONLY
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STEP = 1
    }
}
