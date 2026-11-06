package com.example.delibuddy.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.editBankName.setText(viewModel.bankName.value)
        binding.editAccount.setText(viewModel.account.value)
        binding.editBankName.doAfterTextChanged { viewModel.setBankName(it?.toString().orEmpty()) }
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
            binding.editBankName.isEnabled = !saving
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
