package com.example.delibuddy.ui.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentSigninBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        binding.buttonSignin.setOnClickListener {
            viewModel.signIn(
                binding.editEmail.text?.toString().orEmpty(),
                binding.editPassword.text?.toString().orEmpty(),
            )
        }
        viewModel.isSigningIn.observe(viewLifecycleOwner) { isSigningIn ->
            binding.buttonSignin.isEnabled = !isSigningIn
            binding.editEmail.isEnabled = !isSigningIn
            binding.editPassword.isEnabled = !isSigningIn
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.textSigninStatus.text = message?.let { getString(it) }.orEmpty()
        }
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            val navController = findNavController()
            if (isLoggedIn && navController.currentDestination?.id == R.id.navigation_signin) {
                navController.navigate(R.id.action_signin_to_home)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
