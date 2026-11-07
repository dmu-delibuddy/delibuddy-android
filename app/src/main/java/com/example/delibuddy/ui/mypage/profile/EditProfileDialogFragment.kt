package com.example.delibuddy.ui.mypage.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.profile.ProfileType
import com.example.delibuddy.databinding.DialogEditProfileBinding
import java.util.Calendar
import java.util.Locale

/*
    * 프로필 수정 창 (마이페이지의 "프로필 수정하기")
    * 마이페이지에서 고른 모드(픽업만 / 밥친구)의 프로필을 수정
    * 사진 / 닉네임 / 한 줄 소개 / 생년월일을 고치고 저장 (생년월일은 두 모드 공통)
    * 마이페이지와 같은 ProfileViewModel 을 씀
*/
class EditProfileDialogFragment : DialogFragment() {

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!

    // 부모(마이페이지)의 ProfileViewModel 을 같이 씀
    private val viewModel: ProfileViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    // 새로 고른 사진 (없으면 null)
    private var newImage: Uri? = null

    // 기본 사진으로 되돌리기를 눌렀는지
    private var removeImage = false

    // 고른 생년월일 (yyyy-MM-dd, 없으면 null)
    private var birthDate: String? = null

    // 사진 선택기에서 한 장 고르기
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onImagePicked(it) } }

    // 창 레이아웃(ViewBinding) 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 현재 값 채우기 (회전 후에는 저장해 둔 값 복원) + 버튼 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderTitle()
        if (savedInstanceState == null) fillCurrentValues()
        else restoreState(savedInstanceState)
        renderImage()
        renderBirth()
        setUpImageButtons()
        setUpBirthPicker()
        setUpBottomButtons()
    }

    // 창 배경을 투명하게, 가로는 화면 폭에 맞춤 (픽셀 박스 배경이 보이게)
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    // 창 제목에 수정 중인 모드 표시 (예 : 픽업만 프로필 수정)
    private fun renderTitle() {
        val mode = getString(
            if (viewModel.selectedType.value == ProfileType.MEAL_BUDDY) R.string.mode_meal_buddy
            else R.string.mode_pickup
        )
        binding.textTitle.text = getString(R.string.mypage_edit_profile_title_mode, mode)
    }

    // 지금 프로필 / 생년월일을 입력칸에 채우기
    private fun fillCurrentValues() {
        val profile = viewModel.profile.value
        binding.editNickname.setText(profile?.nickname.orEmpty())
        binding.editBio.setText(profile?.bio.orEmpty())
        birthDate = viewModel.birthDate.value
    }

    // 화면 회전 등으로 다시 만들어질 때 고른 사진 / 생년월일 복원
    private fun restoreState(state: Bundle) {
        newImage = BundleCompat.getParcelable(state, KEY_IMAGE, Uri::class.java)
        removeImage = state.getBoolean(KEY_REMOVE_IMAGE)
        birthDate = state.getString(KEY_BIRTH)
    }

    // 고른 사진 / 생년월일 저장 (입력칸 글자는 EditText 가 스스로 복원)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_IMAGE, newImage)
        outState.putBoolean(KEY_REMOVE_IMAGE, removeImage)
        outState.putString(KEY_BIRTH, birthDate)
    }

    // 사진 칸 누르면 사진 선택기, "기본 사진으로" 누르면 사진 지우기
    private fun setUpImageButtons() {
        binding.layoutProfileImage.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
        binding.buttonRemoveImage.setOnClickListener {
            newImage = null
            removeImage = true
            renderImage()
        }
    }

    // 사진을 골랐을 때
    private fun onImagePicked(uri: Uri) {
        newImage = uri
        removeImage = false
        renderImage()
    }

    // 새 사진 > 기본 사진(지움) > 지금 프로필 사진 순서로 표시
    // 보여줄 사진이 있을 때만 "기본 사진으로" 버튼 표시
    private fun renderImage() {
        val image = binding.imageProfile
        val currentUrl = viewModel.profile.value?.imageUrl.orEmpty()
        val source: Any? = when {
            newImage != null -> newImage
            removeImage || currentUrl.isBlank() -> null
            else -> currentUrl
        }
        if (source == null) {
            Glide.with(image).clear(image)
            image.setImageResource(R.drawable.mypage_icon)
        } else {
            Glide.with(image)
                .load(source)
                .placeholder(R.drawable.mypage_icon)
                .into(image)
        }
        binding.buttonRemoveImage.isVisible = source != null
    }

    // 생년월일 칸 누르면 달력 (미래 날짜는 못 고름)
    private fun setUpBirthPicker() {
        binding.textBirth.setOnClickListener { showBirthPicker() }
    }

    // 고른 날짜가 있으면 그 날짜, 없으면 2000년 1월 1일로 달력 열기
    private fun showBirthPicker() {
        val parts = birthDate?.split("-")?.mapNotNull { it.toIntOrNull() }
        val year = parts?.getOrNull(0) ?: DEFAULT_YEAR
        val month = parts?.getOrNull(1)?.minus(1) ?: 0
        val day = parts?.getOrNull(2) ?: 1
        DatePickerDialog(requireContext(), { _, y, m, d ->
            birthDate = String.format(Locale.ROOT, "%04d-%02d-%02d", y, m + 1, d)
            renderBirth()
        }, year, month, day).apply {
            datePicker.maxDate = Calendar.getInstance().timeInMillis
        }.show()
    }

    // 생년월일 표시 (없으면 힌트가 보임)
    private fun renderBirth() {
        binding.textBirth.text = birthDate
            ?.let { ProfileViewModel.formatBirth(it) }
            .orEmpty()
    }

    // 취소 / 저장 (닉네임 비어 있으면 저장 안 함)
    private fun setUpBottomButtons() {
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { save() }
    }

    // 입력 검사 후 저장 요청. 결과 안내는 마이페이지가 토스트로 보여줌
    private fun save() {
        val nickname = binding.editNickname.text?.toString().orEmpty()
        val error = viewModel.validate(nickname)
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.save(
            nickname = nickname,
            bio = binding.editBio.text?.toString().orEmpty(),
            birthDate = birthDate,
            newImage = newImage,
            removeImage = removeImage,
        )
        dismiss()
    }

    // 창이 사라질 때 binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditProfileDialog"
        private const val KEY_IMAGE = "newImage"
        private const val KEY_REMOVE_IMAGE = "removeImage"
        private const val KEY_BIRTH = "birthDate"
        private const val DEFAULT_YEAR = 2000
    }
}
