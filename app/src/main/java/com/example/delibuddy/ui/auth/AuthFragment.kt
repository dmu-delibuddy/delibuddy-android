package com.example.delibuddy.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        viewModel.text.observe(viewLifecycleOwner) {
            binding.textAuth.text = it
        }

        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            val navController = findNavController()
            if (isLoggedIn && navController.currentDestination?.id == R.id.navigation_auth) {
                navController.navigate(R.id.action_auth_to_home)
            }
        }

        binding.buttonAuthStart.setOnClickListener {
            findNavController().navigate(R.id.action_auth_to_signup)
        }
        binding.buttonAuthSignin.setOnClickListener {
            findNavController().navigate(R.id.action_auth_to_signin)
        }
    }

    override fun onStart() {
        super.onStart()
        ViewModelProvider(this)[AuthViewModel::class.java].checkLoginStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
