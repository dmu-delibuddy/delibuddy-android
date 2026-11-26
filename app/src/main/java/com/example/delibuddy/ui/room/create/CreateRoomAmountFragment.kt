package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.databinding.FragmentCreateRoomAmountBinding
import com.example.delibuddy.ui.room.create.CreateRoomViewModel.SubmitState

class CreateRoomAmountFragment : Fragment() {

    private var _binding: FragmentCreateRoomAmountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateRoomViewModel by navGraphViewModels(R.id.navigation_create_room)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateRoomAmountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCreateRoomHeader(binding.header, STEP)
        setUpCreateRoomBottom(binding.bottom, STEP) { viewModel.submit() }
        setUpAmountInputs()
        setUpFoodSplitSelect()
        observeFoodSplit()
        setUpDeliverySplitSelect()
        observeFeeNote()
        observeSubmitState()
    }

    private fun setUpAmountInputs() {
        binding.minOrderAmount.editText.doAfterTextChanged {
            viewModel.setMinOrderAmount(it?.toString()?.toIntOrNull())
        }
        binding.deliveryFeeAmount.editText.doAfterTextChanged {
            viewModel.setDeliveryFee(it?.toString()?.toIntOrNull())
        }
    }

    private fun setUpFoodSplitSelect() {
        val ids = mapOf(
            R.id.radio_food_host to FoodCostSplitMethod.HOST_PAYS,
            R.id.radio_food_equal to FoodCostSplitMethod.EQUAL_SPLIT,
            R.id.radio_food_by_menu to FoodCostSplitMethod.PROPORTIONAL_SPLIT,
            R.id.radio_food_custom to FoodCostSplitMethod.CUSTOM,
        )
        binding.groupFoodSplit.setOnCheckedChangeListener { _, id ->
            ids[id]?.let { viewModel.setFoodCostSplit(it) }
        }
    }

    private fun observeFoodSplit() {
        viewModel.foodCostSplit.observe(viewLifecycleOwner) { method ->
            binding.textFoodSplitDesc.setText(
                when (method) {
                    FoodCostSplitMethod.HOST_PAYS -> R.string.room_food_split_host_description
                    FoodCostSplitMethod.EQUAL_SPLIT -> R.string.room_food_split_equal_description
                    FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.room_food_split_by_menu_description
                    FoodCostSplitMethod.CUSTOM -> R.string.room_food_split_custom_description
                }
            )
        }
    }

    private fun setUpDeliverySplitSelect() {
        val ids = mapOf(
            R.id.radio_delivery_host to DeliveryFeeSplitMethod.HOST_PAYS,
            R.id.radio_delivery_equal to DeliveryFeeSplitMethod.EQUAL_SPLIT,
            R.id.radio_delivery_custom to DeliveryFeeSplitMethod.CUSTOM,
        )
        binding.groupDeliverySplit.setOnCheckedChangeListener { _, id ->
            ids[id]?.let { viewModel.setDeliveryFeeSplit(it) }
        }
    }

    private fun observeFeeNote() {
        viewModel.deliveryFee.observe(viewLifecycleOwner) { renderFeeNote() }
        viewModel.maxParticipants.observe(viewLifecycleOwner) { renderFeeNote() }
        viewModel.deliveryFeeSplit.observe(viewLifecycleOwner) { renderFeeNote() }
    }

    private fun renderFeeNote() {
        val fee = viewModel.deliveryFee.value
        val people = viewModel.maxParticipants.value ?: CreateRoomViewModel.DEFAULT_PARTICIPANTS
        binding.textFeeNote.text = when (viewModel.deliveryFeeSplit.value) {
            DeliveryFeeSplitMethod.HOST_PAYS -> getString(R.string.room_create_fee_note_host)
            DeliveryFeeSplitMethod.CUSTOM -> getString(R.string.room_create_fee_note_custom)
            else -> if (fee == null) "" else
                getString(R.string.room_create_fee_note_equal, people, "%,d".format(fee / people))
        }
    }

    private fun observeSubmitState() {
        viewModel.submitState.observe(viewLifecycleOwner) { state ->
            renderSubmitButtons(state)
            handleSubmitResult(state)
        }
    }

    private fun renderSubmitButtons(state: SubmitState) {
        val loading = state is SubmitState.Loading
        binding.bottom.buttonNext.isEnabled = !loading
        binding.bottom.buttonPrev.isEnabled = !loading
        binding.bottom.buttonNext.setText(
            if (loading) R.string.room_create_submitting else R.string.room_create_submit
        )
    }

    private fun handleSubmitResult(state: SubmitState) {
        when (state) {
            is SubmitState.Success -> {
                showToast(R.string.room_create_success)
                findNavController().popBackStack(R.id.navigation_create_room, true)
            }

            is SubmitState.Error -> {
                showToast(state.message)
                viewModel.clearSubmitError()
                stepDestination(state.step)?.let {
                    findNavController().popBackStack(it, false)
                }
            }

            else -> Unit
        }
    }

    private fun stepDestination(step: Int?): Int? = when (step) {
        1 -> R.id.navigation_create_room_info
        2 -> R.id.navigation_create_room_food
        3 -> R.id.navigation_create_room_place
        else -> null
    }

    private fun showToast(@StringRes message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STEP = 4
    }
}
