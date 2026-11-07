package com.example.delibuddy.ui.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentSigninBinding
import com.example.delibuddy.ui.common.GlobalLoadingDialog

class SignInFragment : Fragment() {

    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignInViewModel by viewModels()

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

        setUpSignInButton()
        observeSignInState()
        observeMessages()
        observeLoginSuccess()
    }

    private fun setUpSignInButton() {
        binding.buttonSignin.setOnClickListener {
            viewModel.signIn(
                binding.editEmail.text?.toString().orEmpty(),
                binding.editPassword.text?.toString().orEmpty(),
            )
        }
    }

    private fun observeSignInState() {
        viewModel.isSigningIn.observe(viewLifecycleOwner) { isSigningIn ->
            binding.buttonSignin.isEnabled = !isSigningIn
            binding.editEmail.isEnabled = !isSigningIn
            binding.editPassword.isEnabled = !isSigningIn
            if (isSigningIn) {
                GlobalLoadingDialog.show(childFragmentManager, getString(R.string.signin_loading))
            } else {
                GlobalLoadingDialog.hide(childFragmentManager)
            }
        }
    }

    private fun observeMessages() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.textSigninStatus.text = message?.let { getString(it) }.orEmpty()
        }
    }

    private fun observeLoginSuccess() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            val navController = findNavController()
            if (isLoggedIn && navController.currentDestination?.id == R.id.navigation_signin) {
                GlobalLoadingDialog.hide(childFragmentManager)
                navController.navigate(R.id.action_signin_to_home)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
