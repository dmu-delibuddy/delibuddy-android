package com.example.delibuddy.ui.room.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentCreateRoomBinding


/*
    * 방 만들기.
    * - 상단 < : 방 만들기 종료
    * - 이전 / 시스템 뒤로가기 : 이전 단계 (1단계에서는 종료)
*/

class CreateRoomFragment : Fragment() {

    private var _binding : FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this)[CreateRoomViewModel::class.java]

        val progress = listOf(binding.progress1, binding.progress2, binding.progress3, binding.progress4)
        val steps = listOf(binding.step1.root, binding.step2.root, binding.step3.root, binding.step4.root)

        // 시스템 뒤로가기 : 2단계 이상이면 이전 단계로 (1단계에서는 꺼져 있어서 화면 종료)
        val backCallback = requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, enabled = false) { viewModel.prev() }

        // 단계가 바뀔 때마다 화면 갱신
        viewModel.step.observe(viewLifecycleOwner) { step ->
            val last = CreateRoomViewModel.LAST_STEP
            binding.textStep.text = "$step/$last"
            progress.forEachIndexed { i, bar ->
                bar.setBackgroundResource(if (i < step) R.drawable.bg_progress_on else R.drawable.bg_progress_off)
            }
            steps.forEachIndexed { i, page ->
                page.visibility = if (i == step - 1) View.VISIBLE else View.GONE
            }
            binding.buttonPrev.visibility = if (step > 1) View.VISIBLE else View.GONE
            binding.buttonNext.setText(if (step == last) R.string.create_room_submit else R.string.create_room_next)
            binding.scrollContent.scrollTo(0, 0)
            backCallback.isEnabled = step > 1
        }

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonPrev.setOnClickListener { viewModel.prev() }
        binding.buttonNext.setOnClickListener {
            if (viewModel.step.value == CreateRoomViewModel.LAST_STEP) {
                // 입력 검사 + createRoom 연결 예정
                Toast.makeText(requireContext(), "방 개설은 다음 작업에서 연결", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.next()
            }
        }

        // 하단 탭이 없는 화면이라 버튼이 제스처 바에 가리지 않게 아래 여백 추가
        val baseBottom = binding.layoutBottom.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutBottom) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.updatePadding(bottom = baseBottom + navBar)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}