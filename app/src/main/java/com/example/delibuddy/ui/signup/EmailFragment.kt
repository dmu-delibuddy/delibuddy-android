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
import com.example.delibuddy.databinding.FragmentEmailBinding

class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by navGraphViewModels(R.id.navigation_signup)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNextButton()
        setUpEmailInputListener()
        observeEmail()
    }

    private fun setUpNextButton() {
        binding.buttonNext.setOnClickListener {
            findNavController().navigate(R.id.action_email_to_password)
        }
    }

    private fun setUpEmailInputListener() {
        binding.editEmail.doAfterTextChanged { text ->
            viewModel.setEmail(text?.toString().orEmpty())
        }
    }

    private fun observeEmail() {
        viewModel.email.observe(viewLifecycleOwner) { email ->
            if (binding.editEmail.text?.toString() != email) {
                binding.editEmail.setText(email)
                binding.editEmail.setSelection(email.length)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
