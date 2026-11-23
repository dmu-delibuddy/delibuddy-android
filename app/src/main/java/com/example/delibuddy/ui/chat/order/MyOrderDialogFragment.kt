package com.example.delibuddy.ui.chat.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.order.OrderModel
import com.example.delibuddy.databinding.DialogMyOrderBinding
import com.example.delibuddy.ui.chat.order.MyOrderViewModel.LoadState
import com.example.delibuddy.ui.chat.order.MyOrderViewModel.SaveState
import com.example.delibuddy.ui.common.GlobalLoadingDialog

/*
    * 채팅방 > 메뉴 > 내 주문
    * 내 음식값 / 장바구니 사진 / 메모를 확인하고, 모집 중이면 바꿔서 저장
    * 입력 칸은 방 상세의 주문 카드(layout_room_detail_order)를 그대로 씀
*/
class MyOrderDialogFragment : DialogFragment() {

    private var _binding: DialogMyOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyOrderViewModel by viewModels()

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let(viewModel::selectPhoto)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogMyOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListeners()
        observeLoadState()
        observeNewPhoto()
        observeSaveState()
        viewModel.load()
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

    // 사진 고르기, 메모 글자 수, 취소 / 저장
    private fun setUpListeners() = with(binding) {
        order.imageOrderPreview.setOnClickListener { photoPicker.launch("image/*") }
        order.inputOrderMemo.doAfterTextChanged {
            order.textMemoCount.text = getString(R.string.my_order_memo_count, it?.length ?: 0)
        }
        buttonCancel.setOnClickListener { dismiss() }
        buttonSave.setOnClickListener {
            viewModel.save(
                price = order.orderPriceAmount.editText.text.toString().trim().toLongOrNull(),
                memo = order.inputOrderMemo.text.toString(),
                resolver = requireContext().contentResolver,
            )
        }
    }

    // 불러온 주문 채우기 + 모집 중이 아니면 보기 전용
    private fun observeLoadState() {
        viewModel.loadState.observe(viewLifecycleOwner) { state ->
            val loaded = state as? LoadState.Loaded
            val editable = loaded?.editable == true
            with(binding.order) {
                orderPriceAmount.editText.isEnabled = editable
                inputOrderMemo.isEnabled = editable
                imageOrderPreview.isEnabled = editable
                textOrderMessage.text = when (state) {
                    LoadState.Loading -> getString(R.string.my_order_loading)
                    LoadState.Error -> getString(R.string.profile_order_error)
                    is LoadState.Loaded ->
                        if (editable) "" else getString(R.string.my_order_locked)
                }
            }
            binding.buttonSave.isVisible = editable
            binding.buttonCancel.setText(
                if (editable) R.string.my_order_cancel else R.string.my_order_close
            )
            loaded?.order?.let(::fillOrder)
        }
    }

    // 저장된 주문 채우기 (입력 중인 값이 있으면 그대로 둠)
    private fun fillOrder(order: OrderModel) = with(binding.order) {
        val price = orderPriceAmount.editText
        if (price.text.isEmpty()) price.setText(order.price.toString())
        if (inputOrderMemo.text.isEmpty()) inputOrderMemo.setText(order.memo.orEmpty())
        val url = order.cartScreenshotUrl
        if (viewModel.newPhoto.value == null && !url.isNullOrBlank()) {
            Glide.with(this@MyOrderDialogFragment).load(url).into(imageOrderPreview)
        }
    }

    // 새로 고른 사진 미리보기
    private fun observeNewPhoto() {
        viewModel.newPhoto.observe(viewLifecycleOwner) { uri ->
            uri ?: return@observe
            Glide.with(this).load(uri).into(binding.order.imageOrderPreview)
        }
    }

    // 저장 중 로딩, 성공하면 닫기, 실패하면 안내
    private fun observeSaveState() {
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SaveState.Saving ->
                    GlobalLoadingDialog.show(childFragmentManager, getString(R.string.my_order_saving))
                SaveState.Saved -> {
                    GlobalLoadingDialog.hide(childFragmentManager)
                    Toast.makeText(requireContext(), R.string.my_order_saved, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is SaveState.Failed -> {
                    GlobalLoadingDialog.hide(childFragmentManager)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearSaveState()
                }
                SaveState.Idle -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MyOrderDialog"

        fun show(fragmentManager: FragmentManager, roomId: String) {
            if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(TAG) != null) return
            MyOrderDialogFragment().apply {
                arguments = bundleOf(MyOrderViewModel.ARG_ROOM_ID to roomId)
            }.show(fragmentManager, TAG)
        }
    }
}
