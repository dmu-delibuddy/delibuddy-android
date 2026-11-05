package com.example.delibuddy.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentPasswordBinding
import com.example.delibuddy.ui.common.GlobalLoadingDialog

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by navGraphViewModels(R.id.navigation_signup)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPreviousButton()
        setUpVerifyEmailButton()
        setUpVerificationCompleteButton()
        observeSignUpState()
        setUpPasswordInputs()
    }

    private fun setUpPreviousButton() {
        binding.buttonPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpVerifyEmailButton() {
        binding.buttonVerifyEmail.setOnClickListener {
            viewModel.sendVerificationEmail()
        }
    }

    private fun setUpVerificationCompleteButton() {
        binding.buttonVerificationComplete.setOnClickListener {
            viewModel.completeSignUp()
        }
    }

    private fun observeSignUpState() {
        viewModel.isCompletingSignUp.observe(viewLifecycleOwner) {
            updateLoadingState()
        }
        viewModel.isSendingVerification.observe(viewLifecycleOwner) { isSending ->
            updateLoadingState()
            if (isSending) {
                GlobalLoadingDialog.show(
                    childFragmentManager,
                    getString(R.string.signup_sending_verification_email),
                )
            } else {
                GlobalLoadingDialog.hide(childFragmentManager)
            }
        }
        viewModel.verificationMessage.observe(viewLifecycleOwner) { message ->
            binding.textVerificationStatus.text = message?.let { getString(it) }.orEmpty()
            binding.textVerificationStatus.isVisible = message != null
        }
        viewModel.isVerificationEmailSent.observe(viewLifecycleOwner) { sent ->
            binding.textVerificationGuide.isVisible = sent
        }
    }

    private fun setUpPasswordInputs() {
        binding.editPassword.setText(viewModel.password.value)
        binding.editPasswordConfirm.setText(viewModel.passwordConfirm.value)

        binding.editPassword.doAfterTextChanged { text ->
            viewModel.setPassword(text?.toString().orEmpty())
        }
        binding.editPasswordConfirm.doAfterTextChanged { text ->
            viewModel.setPasswordConfirm(text?.toString().orEmpty())
        }
    }

    private fun updateLoadingState() {
        val isBusy = viewModel.isSendingVerification.value == true ||
            viewModel.isCompletingSignUp.value == true
        binding.buttonVerificationComplete.isEnabled = !isBusy
        binding.buttonVerifyEmail.isEnabled = !isBusy
        binding.buttonPrevious.isEnabled = !isBusy
        binding.editPassword.isEnabled = !isBusy
        binding.editPasswordConfirm.isEnabled = !isBusy
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
