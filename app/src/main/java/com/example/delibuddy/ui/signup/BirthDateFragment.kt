package com.example.delibuddy.ui.signup

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentBirthDateBinding
import java.util.Calendar
import java.util.Locale

class BirthDateFragment : Fragment() {

    private var _binding: FragmentBirthDateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by navGraphViewModels(R.id.navigation_signup)
    private var datePicker: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBirthDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBirthDate.setOnClickListener { showDatePicker() }
        viewModel.birthDate.observe(viewLifecycleOwner) { date ->
            binding.buttonBirthDate.text = date ?: getString(R.string.signup_birth_date_hint)
        }
    }

    private fun showDatePicker() {
        if (datePicker?.isShowing == true) return
        val initial = Calendar.getInstance()
        val selectedDate = viewModel.birthDate.value
        if (selectedDate == null) {
            initial.add(Calendar.YEAR, -20)
        } else {
            val parts = selectedDate.split("-")
            initial.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
        datePicker = DatePickerDialog(
            requireContext(),
            R.style.BirthDatePickerDialog,
            { _, year, month, day ->
                viewModel.setBirthDate(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day))
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH),
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    override fun onDestroyView() {
        datePicker?.dismiss()
        datePicker = null
        _binding = null
        super.onDestroyView()
    }
}
