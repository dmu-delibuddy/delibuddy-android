package com.example.delibuddy.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.profile.ProfileModel
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.databinding.FragmentMypageBinding
import com.example.delibuddy.ui.mypage.account.AccountViewModel
import com.example.delibuddy.ui.mypage.account.EditAccountDialogFragment
import com.example.delibuddy.ui.mypage.profile.EditProfileDialogFragment
import com.example.delibuddy.ui.mypage.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

/*
    * 마이페이지 (화면 틀)
    * - 프로필 카드 : profile 패키지 (ProfileViewModel, 프로필 수정 창)
    * - 송금 계좌 카드 : account 패키지 (AccountViewModel, 계좌 변경 창)
    * - 로그아웃
*/
class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    // 화면 레이아웃(ViewBinding) 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 도메인별(프로필 / 계좌) 버튼 연결과 구독 시작
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프로필
        setUpModeSelect()
        setUpEditProfileButton()
        observeSelectedMode()
        observeProfile()
        observeBirthDate()
        observeMessages(profileViewModel.message) { profileViewModel.clearMessage() }

        // 송금 계좌
        setUpChangeAccountButton()
        observeAccount()
        observeMessages(accountViewModel.message) { accountViewModel.clearMessage() }

        // 기타
        setUpLogoutButton()
    }

    // ---------------- 프로필 ----------------

    // 모드 선택 (픽업만 / 밥친구) : 그 모드의 프로필을 보여줌
    private fun setUpModeSelect() {
        binding.cardProfile.groupMode.setOnCheckedChangeListener { _, checkedId ->
            profileViewModel.selectMode(
                if (checkedId == R.id.radio_meal_buddy) ProfileType.MEAL_BUDDY
                else ProfileType.PICKUP_ONLY
            )
        }
    }

    // "프로필 수정하기" : 프로필 수정 창 열기
    private fun setUpEditProfileButton() {
        binding.cardProfile.buttonEditProfile.setOnClickListener {
            EditProfileDialogFragment()
                .show(childFragmentManager, EditProfileDialogFragment.TAG)
        }
    }

    // 고른 모드에 맞게 모드 버튼 체크
    private fun observeSelectedMode() {
        profileViewModel.selectedType.observe(viewLifecycleOwner) { type ->
            val modeId =
                if (type == ProfileType.MEAL_BUDDY) R.id.radio_meal_buddy
                else R.id.radio_pickup
            val group = binding.cardProfile.groupMode
            if (group.checkedRadioButtonId != modeId) group.check(modeId)
        }
    }

    // 프로필이 바뀔 때마다 카드 다시 그리기
    private fun observeProfile() {
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            renderProfile(profile)
        }
    }

    // 닉네임, 소개, 사진 표시 (비어 있으면 안내 문구 / 기본 사진)
    private fun renderProfile(profile: ProfileModel) = with(binding.cardProfile) {
        textNickname.text = profile.nickname
            .ifBlank { getString(R.string.mypage_nickname_empty) }
        textBio.text = profile.bio.orEmpty()
            .ifBlank { getString(R.string.mypage_bio_empty) }

        if (profile.imageUrl.isNullOrBlank()) {
            Glide.with(imageProfile).clear(imageProfile)
            imageProfile.setImageResource(R.drawable.mypage_icon)
        } else {
            Glide.with(imageProfile)
                .load(profile.imageUrl)
                .placeholder(R.drawable.mypage_icon)
                .into(imageProfile)
        }
    }

    // 생일 표시 (2004-03-15 -> 2004. 03. 15, 없으면 "미입력")
    private fun observeBirthDate() {
        profileViewModel.birthDate.observe(viewLifecycleOwner) { birthDate ->
            binding.cardProfile.textBirth.text = birthDate
                ?.let { ProfileViewModel.formatBirth(it) }
                ?: getString(R.string.mypage_birth_empty)
        }
    }

    // ---------------- 송금 계좌 ----------------

    // "변경" : 계좌 변경 창 열기
    private fun setUpChangeAccountButton() {
        binding.cardAccount.buttonChangeAccount.setOnClickListener {
            EditAccountDialogFragment()
                .show(childFragmentManager, EditAccountDialogFragment.TAG)
        }
    }

    // 계좌 표시 (등록 안 했으면 안내 문구)
    private fun observeAccount() {
        accountViewModel.account.observe(viewLifecycleOwner) { account ->
            binding.cardAccount.textAccount.text =
                if (account.isRegistered) "${account.bankName} ${account.number}"
                else getString(R.string.mypage_account_empty)
        }
    }

    // ---------------- 공통 ----------------

    // 각 도메인의 안내 문구를 토스트로 보여준 뒤 비우기
    private fun observeMessages(
        message: LiveData<Int?>,
        clear: () -> Unit,
    ) {
        message.observe(viewLifecycleOwner) { resId ->
            if (resId != null) {
                Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
                clear()
            }
        }
    }

    // 로그아웃 : 첫 화면으로 이동하고 이전 화면 기록 지우기
    private fun setUpLogoutButton() {
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

    // 화면이 사라질 때 binding 해제 (메모리 누수 방지)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
