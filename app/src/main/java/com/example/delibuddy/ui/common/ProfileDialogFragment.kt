package com.example.delibuddy.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.databinding.DialogProfileBinding

/*
    * 다른 사용자의 프로필을 보여주는 모달.
    * 사용법 (Fragment 안에서):
    *     ProfileDialogFragment.show(childFragmentManager, userId, ProfileType.PICKUP_ONLY)
*/
class ProfileDialogFragment : DialogFragment() {

    private var _binding: DialogProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClose.setOnClickListener { dismiss() }
        viewModel.state.observe(viewLifecycleOwner) { render(it) }
        val args = requireArguments()
        viewModel.load(
            args.getString(ARG_USER_ID).orEmpty(),
            ProfileType.valueOf(args.getString(ARG_TYPE) ?: ProfileType.PICKUP_ONLY.name)
        )
    }

    override fun onStart() {
        super.onStart()
        // 화면 가로의 90% 폭, 배경은 투명(모서리 둥글게 하고 싶을 때 대비)
        dialog?.window?.apply {
            setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun render(state: ProfileUiState) {
        with(binding) {
            progress.isVisible = state is ProfileUiState.Loading
            layoutContent.isVisible = state is ProfileUiState.Success
            textMessage.isVisible = state is ProfileUiState.NotFound || state is ProfileUiState.Error

            when (state) {
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    textNickname.text = profile.nickname.ifBlank { "이름 없음" }
                    textMode.text = when (profile.type) {
                        ProfileType.PICKUP_ONLY -> "픽업 모드"
                        ProfileType.MEAL_BUDDY -> "밥친구 모드"
                    }
                    textBio.text = profile.bio.ifBlank { "소개가 없어요" }
                    // TODO: 리뷰(reviews) 기능이 생기면 받은 리뷰 평균 별점으로 교체
                    textManner.text = "매너 점수  -"
                    // TODO: imageUrl 이미지 로딩 (이미지 라이브러리 결정 후)
                }
                is ProfileUiState.NotFound -> textMessage.text = "프로필이 없는 사용자예요"
                is ProfileUiState.Error -> textMessage.text = "불러오지 못했어요.\n잠시 후 다시 시도해 주세요"
                is ProfileUiState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProfileDialog"
        private const val ARG_USER_ID = "userId"
        private const val ARG_TYPE = "type"

        // type : 보여줄 프로필 모드 (사용자는 모드별로 프로필이 따로 있음)
        fun show(fragmentManager: FragmentManager, userId: String, type: ProfileType) {
            ProfileDialogFragment().apply {
                arguments = bundleOf(ARG_USER_ID to userId, ARG_TYPE to type.name)
            }.show(fragmentManager, TAG)
        }
    }
}
