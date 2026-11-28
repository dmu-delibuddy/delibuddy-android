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
import com.example.delibuddy.ui.common.GlobalAlertDialog
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

        binding.signupProgress.setStep(STEP)
        setUpButtonListeners()
        observeSignUpState()
        observeVerificationCompletion()
        setUpPasswordInputs()
    }

    private fun setUpButtonListeners() {
        binding.buttonPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonVerifyEmail.setOnClickListener {
            viewModel.sendVerificationEmail()
        }
        binding.buttonVerificationComplete.setOnClickListener {
            viewModel.confirmEmailVerification()
        }
    }

    private fun observeSignUpState() {
        viewModel.isConfirmingVerification.observe(viewLifecycleOwner) {
            updateLoadingState()
        }
        viewModel.isSendingVerification.observe(viewLifecycleOwner) {
            updateLoadingState()
        }
        viewModel.verificationMessage.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            GlobalLoadingDialog.hide(childFragmentManager)
            GlobalAlertDialog.show(
                fragmentManager = childFragmentManager,
                type = GlobalAlertDialog.Type.INFO,
                title = getString(message),
            )
            viewModel.onVerificationMessageShown()
        }
        viewModel.isVerificationEmailSent.observe(viewLifecycleOwner) { sent ->
            binding.textVerificationGuide.isVisible = sent
            updateLoadingState()
        }
        viewModel.isEmailVerified.observe(viewLifecycleOwner) { verified ->
            binding.textVerificationDone.isVisible = verified
        }

    }

    private fun observeVerificationCompletion() {
        viewModel.isReadyForBirthDate.observe(viewLifecycleOwner) { isReady ->
            val navController = findNavController()
            if (isReady && navController.currentDestination?.id == R.id.navigation_password) {
                GlobalLoadingDialog.hide(childFragmentManager)
                navController.navigate(R.id.action_password_to_birth_date)
                viewModel.onBirthDateNavigated()
            }
        }
    }

    private fun setUpPasswordInputs() {
        binding.editPassword.setText(viewModel.password)
        binding.editPasswordConfirm.setText(viewModel.passwordConfirm)

        binding.editPassword.doAfterTextChanged { text ->
            viewModel.setPassword(text?.toString().orEmpty())
        }
        binding.editPasswordConfirm.doAfterTextChanged { text ->
            viewModel.setPasswordConfirm(text?.toString().orEmpty())
        }
    }

    private fun updateLoadingState() {
        val isBusy = viewModel.isSendingVerification.value == true ||
                viewModel.isConfirmingVerification.value == true
        val isSent = viewModel.isVerificationEmailSent.value == true
        binding.buttonVerificationComplete.isEnabled = !isBusy && isSent
        binding.buttonVerifyEmail.isEnabled = !isBusy && !isSent
        binding.buttonPrevious.isEnabled = !isBusy
        binding.editPassword.isEnabled = !isBusy
        binding.editPasswordConfirm.isEnabled = !isBusy

        val loadingMessage = when {
            viewModel.isConfirmingVerification.value == true -> R.string.signup_verification_checking
            viewModel.isSendingVerification.value == true -> R.string.signup_verification_email_sending
            else -> null
        }
        if (loadingMessage != null) {
            GlobalLoadingDialog.show(childFragmentManager, getString(loadingMessage))
        } else {
            GlobalLoadingDialog.hide(childFragmentManager)
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
