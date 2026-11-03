package com.example.delibuddy.ui.authentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentAuthEntryBinding

class AuthEntryFragment : Fragment() {

    private var _binding: FragmentAuthEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthEntryViewModel by viewModels()

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

        observeLoginChecking()
        observeLoginStatus()
        setUpSignUpButton()
        setUpSignInButton()
    }

    private fun observeLoginChecking() {
        viewModel.isChecking.observe(viewLifecycleOwner) { isChecking ->
            binding.buttonAuthEntryStart.isEnabled = !isChecking
            binding.buttonAuthEntrySignin.isEnabled = !isChecking
        }
    }

    private fun observeLoginStatus() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { canEnterHome ->
            val navController = findNavController()
            if (canEnterHome && navController.currentDestination?.id == R.id.navigation_auth_entry) {
                navController.navigate(R.id.action_auth_entry_to_home)
            }
        }
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

    override fun onStart() {
        super.onStart()
        viewModel.checkLoginStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
