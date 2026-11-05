package com.example.delibuddy.ui.authentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentAuthEntryBinding

class AuthEntryFragment : Fragment() {

    private var _binding: FragmentAuthEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSignUpButton()
        setUpSignInButton()
    }

    private fun setUpSignUpButton() {
        binding.buttonAuthEntryStart.setOnClickListener {
            findNavController().navigate(R.id.action_auth_entry_to_signup)
        }
    }

    private fun setUpSignInButton() {
        binding.buttonAuthEntrySignin.setOnClickListener {
            findNavController().navigate(R.id.action_auth_entry_to_signin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
