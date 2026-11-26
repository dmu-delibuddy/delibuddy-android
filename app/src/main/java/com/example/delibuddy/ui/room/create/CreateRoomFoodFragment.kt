package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.databinding.FragmentCreateRoomFoodBinding

class CreateRoomFoodFragment : Fragment() {

    private var _binding: FragmentCreateRoomFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateRoomViewModel by navGraphViewModels(R.id.navigation_create_room)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateRoomFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCreateRoomHeader(binding.header, STEP)
        setUpCreateRoomBottom(binding.bottom, STEP) { goNextIfValid() }
        setUpCategorySpinner()
        setUpRestaurantInput()
    }

    private fun goNextIfValid() {
        val error = viewModel.validateStep(STEP)
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(R.id.action_create_room_food_to_place)
    }

    private fun setUpCategorySpinner() {
        val categories = FoodCategory.entries
        val labels = listOf(getString(R.string.room_create_category_hint)) +
                resources.getStringArray(R.array.room_create_category_labels)

        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, labels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.setCategory(if (position == 0) null else categories[position - 1])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun setUpRestaurantInput() {
        binding.editRestaurant.doAfterTextChanged { text ->
            viewModel.setRestaurant(text?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STEP = 2
    }
}
