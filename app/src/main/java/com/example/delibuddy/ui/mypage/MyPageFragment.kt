package com.example.delibuddy.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this)[MyPageViewModel::class.java]

        viewModel.text.observe(viewLifecycleOwner) {
            binding.textMypage.text = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 개발용 임시 버튼
        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(
                R.id.navigation_auth_entry,
                null,
                navOptions {
                    popUpTo(R.id.mobile_navigation) { inclusive = false }
                    launchSingleTop = true
                },
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
