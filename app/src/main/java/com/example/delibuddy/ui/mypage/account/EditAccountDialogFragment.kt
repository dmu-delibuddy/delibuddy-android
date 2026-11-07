package com.example.delibuddy.ui.mypage.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.DialogEditAccountBinding

/*
    * 송금 계좌 변경 창 (마이페이지의 "변경")
    * 은행을 고르고 계좌번호(숫자만)를 입력해 저장
    * 마이페이지와 같은 AccountViewModel 을 씀
*/
class EditAccountDialogFragment : DialogFragment() {

    private var _binding: DialogEditAccountBinding? = null
    private val binding get() = _binding!!

    // 부모(마이페이지)의 AccountViewModel 을 같이 씀
    private val viewModel: AccountViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    // 은행 목록 (strings.xml 의 mypage_bank_list)
    private val banks: Array<String> by lazy {
        resources.getStringArray(R.array.mypage_bank_list)
    }

    // 창 레이아웃(ViewBinding) 생성
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 은행 목록 연결, 현재 값 채우기(처음 열 때만), 버튼 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBankSpinner()
        if (savedInstanceState == null) fillCurrentValues()
        setUpBottomButtons()
    }

    // 창 배경을 투명하게, 가로는 화면 폭에 맞춤
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

    // 은행 선택 목록
    private fun setUpBankSpinner() {
        binding.spinnerBank.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            banks,
        )
    }

    // 지금 은행 / 계좌번호 채우기 (목록에 없는 은행이면 첫 번째)
    private fun fillCurrentValues() {
        val account = viewModel.account.value
        val index = banks.indexOf(account?.bankName)
        if (index >= 0) binding.spinnerBank.setSelection(index)
        binding.editAccount.setText(account?.number.orEmpty())
    }

    // 취소 / 저장
    private fun setUpBottomButtons() {
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { save() }
    }

    // 계좌번호 검사 후 저장 요청. 결과 안내는 마이페이지가 토스트로 보여줌
    private fun save() {
        val account = binding.editAccount.text?.toString()?.trim().orEmpty()
        val error = viewModel.validate(account)
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.save(
            bankName = binding.spinnerBank.selectedItem.toString(),
            number = account,
        )
        dismiss()
    }

    // 창이 사라질 때 binding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditAccountDialog"
    }
}
