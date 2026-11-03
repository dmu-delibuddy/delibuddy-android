package com.example.delibuddy.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentPasswordBinding

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

        binding.buttonPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonVerifyEmail.setOnClickListener {
            viewModel.sendVerificationEmail()
        }
        viewModel.isSendingVerification.observe(viewLifecycleOwner) { isSending ->
            binding.buttonVerifyEmail.isEnabled = !isSending
            binding.buttonPrevious.isEnabled = !isSending
            binding.editPassword.isEnabled = !isSending
            binding.editPasswordConfirm.isEnabled = !isSending
        }
        viewModel.verificationMessage.observe(viewLifecycleOwner) { message ->
            binding.textVerificationStatus.text = message?.let { getString(it) }.orEmpty()
        }

        binding.editPassword.setText(viewModel.password.value)
        binding.editPasswordConfirm.setText(viewModel.passwordConfirm.value)

        binding.editPassword.doAfterTextChanged { text ->
            viewModel.setPassword(text?.toString().orEmpty())
        }
        binding.editPasswordConfirm.doAfterTextChanged { text ->
            viewModel.setPasswordConfirm(text?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
