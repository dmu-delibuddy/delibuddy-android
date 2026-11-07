package com.example.delibuddy.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentAccountInfoBinding
import com.example.delibuddy.ui.common.GlobalLoadingDialog

class AccountInfoFragment : Fragment() {

    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by navGraphViewModels(R.id.navigation_signup)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val banks = resources.getStringArray(R.array.mypage_bank_list)
        binding.spinnerBank.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, banks,
        )
        val selectedBank = banks.indexOf(viewModel.bankName.value)
        if (selectedBank >= 0) binding.spinnerBank.setSelection(selectedBank)
        binding.spinnerBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setBankName(banks[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setBankName("")
            }
        }
        binding.editAccount.setText(viewModel.account.value)
        binding.editAccount.doAfterTextChanged { viewModel.setAccount(it?.toString().orEmpty()) }
        binding.buttonPrevious.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSkip.setOnClickListener { viewModel.finishRegistration(skipAccount = true) }
        binding.buttonFinish.setOnClickListener { viewModel.finishRegistration(skipAccount = false) }
        observeRegistrationState()
    }

    private fun observeRegistrationState() {
        viewModel.isSavingProfile.observe(viewLifecycleOwner) { saving ->
            binding.buttonPrevious.isEnabled = !saving
            binding.buttonSkip.isEnabled = !saving
            binding.buttonFinish.isEnabled = !saving
            binding.spinnerBank.isEnabled = !saving
            binding.editAccount.isEnabled = !saving
            if (saving) {
                GlobalLoadingDialog.show(childFragmentManager, getString(R.string.signup_saving_profile))
            } else {
                GlobalLoadingDialog.hide(childFragmentManager)
            }
        }
        viewModel.profileError.observe(viewLifecycleOwner) { error ->
            binding.textProfileError.text = error?.let { getString(it) }.orEmpty()
            binding.textProfileError.isVisible = error != null
        }
        viewModel.isRegistrationComplete.observe(viewLifecycleOwner) { complete ->
            val navController = findNavController()
            if (complete && navController.currentDestination?.id == R.id.navigation_account_info) {
                GlobalLoadingDialog.hide(childFragmentManager)
                navController.navigate(R.id.action_account_info_to_home)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
