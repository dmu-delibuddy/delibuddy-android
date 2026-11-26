package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.data.room.DeliveryApp
import com.example.delibuddy.databinding.BottomSheetPlacePickerBinding
import com.example.delibuddy.databinding.FragmentCreateRoomPlaceBinding
import com.example.delibuddy.databinding.ItemPlaceBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class CreateRoomPlaceFragment : Fragment() {

    private var _binding: FragmentCreateRoomPlaceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateRoomViewModel by navGraphViewModels(R.id.navigation_create_room)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreateRoomPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCreateRoomHeader(binding.header, STEP)
        setUpCreateRoomBottom(binding.bottom, STEP) { goNextIfValid() }
        setUpPlaceSelect()
        observePlace()
        setUpDeliveryAppSelect()
        observeDeliveryApp()
    }

    private fun goNextIfValid() {
        val error = viewModel.validateStep(STEP)
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(R.id.action_create_room_place_to_amount)
    }

    private fun setUpPlaceSelect() {
        viewModel.loadPlaces(requireContext().assets)
        binding.textPlace.setOnClickListener { showPlacePicker() }
    }

    private fun observePlace() {
        viewModel.place.observe(viewLifecycleOwner) { place ->
            binding.textPlace.text = place?.name.orEmpty()
        }
    }

    private fun showPlacePicker() {
        val dialog = BottomSheetDialog(requireContext())
        val sheet = BottomSheetPlacePickerBinding.inflate(dialog.layoutInflater)

        viewModel.places.value.orEmpty().forEach { location ->
            val item = ItemPlaceBinding.inflate(dialog.layoutInflater, sheet.layoutPlaces, true)
            item.textPlaceName.text = location.name
            item.textPlaceDesc.text = location.description
            item.textPlaceDesc.isVisible = location.description.isNotBlank()
            item.root.setOnClickListener {
                viewModel.selectPlace(location)
                dialog.dismiss()
            }
        }

        sheet.buttonPlaceCustom.setOnClickListener {
            val name = sheet.editPlace.text?.toString().orEmpty()
            if (name.isBlank()) {
                sheet.editPlace.error = getString(R.string.room_create_error_place_name)
                return@setOnClickListener
            }
            viewModel.setCustomPlace(name)
            dialog.dismiss()
        }

        dialog.setContentView(sheet.root)
        dialog.show()
    }

    private fun setUpDeliveryAppSelect() {
        val ids = mapOf(
            R.id.radio_app_baemin to DeliveryApp.BAEMIN,
            R.id.radio_app_coupang to DeliveryApp.COUPANG_EATS,
            R.id.radio_app_yogiyo to DeliveryApp.YOGIYO,
            R.id.radio_app_other to DeliveryApp.OTHER,
        )
        binding.groupDeliveryApp.setOnCheckedChangeListener { _, id ->
            ids[id]?.let { viewModel.setDeliveryApp(it) }
        }
        binding.editDeliveryApp.doAfterTextChanged { text ->
            viewModel.setDeliveryAppName(text?.toString().orEmpty())
        }
    }

    private fun observeDeliveryApp() {
        viewModel.deliveryApp.observe(viewLifecycleOwner) { app ->
            binding.editDeliveryApp.isVisible = app == DeliveryApp.OTHER
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STEP = 3
    }
}
