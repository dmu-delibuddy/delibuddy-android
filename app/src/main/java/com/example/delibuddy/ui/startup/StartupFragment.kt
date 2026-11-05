package com.example.delibuddy.ui.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentStartupBinding
import com.example.delibuddy.ui.common.PizzaLoadingAnimation

class StartupFragment : Fragment() {

    private var _binding: FragmentStartupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StartupViewModel by viewModels()
    private var animation: PizzaLoadingAnimation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLoadingContent()
        setUpRetryButton()
        setUpLoginButton()
        observeStartupState()
    }

    private fun setUpLoadingContent() {
        binding.loadingContent.loadingMessage.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.navy)
        )
        animation = PizzaLoadingAnimation(
            binding.loadingContent.loadingPizza,
            binding.loadingContent.loadingShadow,
        )
    }

    private fun setUpRetryButton() {
        binding.buttonRetry.setOnClickListener {
            viewModel.checkLoginStatus()
        }
    }

    private fun setUpLoginButton() {
        binding.buttonLogin.setOnClickListener {
            navigate(R.id.action_startup_to_auth_entry)
        }
    }

    private fun observeStartupState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updateLoadingState(state)
            when (state) {
                StartupState.HOME -> navigate(R.id.action_startup_to_home)
                StartupState.AUTH_ENTRY -> navigate(R.id.action_startup_to_auth_entry)
                else -> Unit
            }
        }
    }

    private fun updateLoadingState(state: StartupState) {
        val isError = state == StartupState.ERROR
        binding.buttonRetry.isVisible = isError
        binding.buttonLogin.isVisible = isError
        binding.loadingContent.loadingMessage.setText(
            if (isError) R.string.startup_check_failed else R.string.startup_checking_login,
        )
        if (state == StartupState.CHECKING) {
            animation?.start()
        } else {
            animation?.stop()
        }
    }

    private fun navigate(action: Int) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.navigation_startup) {
            navController.navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.state.value == StartupState.CHECKING) {
            animation?.start()
        }
    }

    override fun onStop() {
        animation?.stop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animation?.stop()
        animation = null
        _binding = null
    }
}
