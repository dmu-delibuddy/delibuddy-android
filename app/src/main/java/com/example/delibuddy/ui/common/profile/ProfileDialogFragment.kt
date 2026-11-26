package com.example.delibuddy.ui.common.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.example.delibuddy.R
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.databinding.DialogProfileBinding
import com.example.delibuddy.ui.common.FullscreenImageDialogFragment
import com.example.delibuddy.ui.common.GlobalLoadingDialog
import com.example.delibuddy.ui.common.loadImage

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
        setUpListeners()
        observeProfile()
        loadProfile()
    }

    private fun setUpListeners() {
        binding.buttonClose.setOnClickListener { dismiss() }
        val args = requireArguments()
        binding.layoutHostActions.isVisible = args.getBoolean(ARG_HOST_ACTIONS)
        val canRemove = args.getBoolean(ARG_CAN_REMOVE)
        binding.buttonRemoveParticipant.isEnabled = canRemove
        binding.buttonViewOrder.setOnClickListener { sendAction(REQUEST_VIEW_ORDER) }
        binding.buttonRemoveParticipant.setOnClickListener { sendAction(REQUEST_REMOVE_PARTICIPANT) }
        binding.imageProfile.setOnClickListener {
            val imageUrl = (viewModel.state.value as? ProfileUiState.Success)
                ?.profile?.imageUrl?.takeIf { it.isNotBlank() }
                ?: return@setOnClickListener
            FullscreenImageDialogFragment.show(childFragmentManager, imageUrl)
        }
    }

    private fun sendAction(request: String) {
        val manager = parentFragmentManager
        val userId = requireArguments().getString(ARG_USER_ID) ?: return
        dismiss()
        manager.setFragmentResult(request, bundleOf(RESULT_USER_ID to userId))
    }

    private fun observeProfile() {
        viewModel.state.observe(viewLifecycleOwner) { render(it) }
    }

    // show()로 넘겨받은 userId와 프로필 모드로 불러온다
    private fun loadProfile() {
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
        // 불러오는 동안에는 전역 로딩 UI(GlobalLoadingDialog)를 쓰고, 모달 본체는 잠깐 숨김
        val loading = state is ProfileUiState.Loading
        if (loading) {
            GlobalLoadingDialog.show(childFragmentManager)
        } else {
            GlobalLoadingDialog.hide(childFragmentManager)
        }

        with(binding) {
            root.isInvisible = loading
            layoutContent.isVisible = state is ProfileUiState.Success
            textMessage.isVisible = state is ProfileUiState.NotFound || state is ProfileUiState.Error

            when (state) {
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    textNickname.text = profile.nickname.ifBlank { getString(R.string.profile_dialog_nickname_empty) }
                    textMode.root.displayedChild = if (profile.type == ProfileType.MEAL_BUDDY) 1 else 0
                    // ProfileModel 의 bio / imageUrl 은 String? 이라 orEmpty() 로 null 처리
                    textBio.text = profile.bio.orEmpty().ifBlank { getString(R.string.profile_dialog_bio_empty) }
                    // TODO: 리뷰(reviews) 기능이 생기면 받은 리뷰 평균 별점으로 교체
                    textManner.text = getString(R.string.profile_dialog_manner, getString(R.string.profile_count_empty))
                    // 사진이 없으면 레이아웃의 기본 사진(mypage_icon)을 그대로 둔다
                    val imageUrl = profile.imageUrl.orEmpty()
                    if (imageUrl.isNotBlank()) {
                        imageProfile.loadImage(imageUrl)
                    }
                }
                is ProfileUiState.NotFound -> textMessage.setText(R.string.profile_dialog_not_found)
                is ProfileUiState.Error -> textMessage.setText(R.string.profile_load_error)
                is ProfileUiState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        // 로딩 중에 닫히면 취소 불가능한 로딩 창이 남지 않도록 정리
        GlobalLoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProfileDialog"
        private const val ARG_USER_ID = "userId"
        private const val ARG_TYPE = "type"
        private const val ARG_HOST_ACTIONS = "hostActions"
        private const val ARG_CAN_REMOVE = "canRemove"
        const val REQUEST_VIEW_ORDER = "profile_view_order"
        const val REQUEST_REMOVE_PARTICIPANT = "profile_remove_participant"
        const val RESULT_USER_ID = "profile_user_id"

        // type : 보여줄 프로필 모드 (사용자는 모드별로 프로필이 따로 있음)
        fun show(
            fragmentManager: FragmentManager,
            userId: String,
            type: ProfileType,
            hostActions: Boolean = false,
            canRemove: Boolean = false,
        ) {
            ProfileDialogFragment().apply {
                arguments = bundleOf(
                    ARG_USER_ID to userId,
                    ARG_TYPE to type.name,
                    ARG_HOST_ACTIONS to hostActions,
                    ARG_CAN_REMOVE to canRemove,
                )
            }.showNow(fragmentManager, TAG)
        }
    }
}
