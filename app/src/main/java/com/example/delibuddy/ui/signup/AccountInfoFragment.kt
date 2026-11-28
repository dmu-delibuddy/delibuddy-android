package com.example.delibuddy.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentAccountInfoBinding
import com.example.delibuddy.ui.common.GlobalAlertDialog
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
        binding.signupProgress.setStep(STEP)
        setUpAccountInput()
        setUpListeners()
        observeRegistrationState()
    }

    private fun setUpAccountInput() {
        val banks = resources.getStringArray(R.array.account_bank_list)
        binding.spinnerBank.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, banks,
        )
        val selectedBank = banks.indexOf(viewModel.bankName)
        if (selectedBank >= 0) binding.spinnerBank.setSelection(selectedBank)
        binding.spinnerBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setBankName(banks[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setBankName("")
            }
        }
        binding.editAccount.setText(viewModel.account)
        binding.editAccount.doAfterTextChanged { viewModel.setAccount(it?.toString().orEmpty()) }
    }

    private fun setUpListeners() {
        binding.buttonPrevious.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSkip.setOnClickListener {
            viewModel.setBankName(null)
            viewModel.setAccount(null)
            viewModel.finishRegistration()
        }
        binding.buttonFinish.setOnClickListener {
            viewModel.setBankName(binding.spinnerBank.selectedItem as? String ?: "")
            viewModel.setAccount(binding.editAccount.text?.toString().orEmpty())
            viewModel.finishRegistration()
        }
    }

    private fun observeRegistrationState() {
        viewModel.isSavingProfile.observe(viewLifecycleOwner) { saving ->
            binding.buttonPrevious.isEnabled = !saving
            binding.buttonSkip.isEnabled = !saving
            binding.buttonFinish.isEnabled = !saving
            binding.spinnerBank.isEnabled = !saving
            binding.editAccount.isEnabled = !saving
            if (saving) {
                GlobalLoadingDialog.show(
                    childFragmentManager,
                    getString(R.string.signup_profile_saving)
                )
            } else {
                GlobalLoadingDialog.hide(childFragmentManager)
            }
        }
        viewModel.profileError.observe(viewLifecycleOwner) { error ->
            if (error == null) return@observe
            GlobalLoadingDialog.hide(childFragmentManager)
            GlobalAlertDialog.show(
                fragmentManager = childFragmentManager,
                type = GlobalAlertDialog.Type.INFO,
                title = getString(error),
            )
            viewModel.onProfileErrorShown()
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

    companion object {
        private const val STEP = 4
    }
}
