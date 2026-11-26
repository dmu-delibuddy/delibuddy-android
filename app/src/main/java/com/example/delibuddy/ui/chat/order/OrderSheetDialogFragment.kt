package com.example.delibuddy.ui.chat.order

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.DialogOrderSheetBinding
import com.example.delibuddy.ui.common.FullscreenImageDialogFragment
import com.example.delibuddy.ui.room.deliveryAppLabel

class OrderSheetDialogFragment : DialogFragment() {

    private var _binding: DialogOrderSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderSheetViewModel by viewModels()
    private lateinit var adapter: OrderSheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogOrderSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpOrderList()
        setUpListeners()
        observeSheet()
    }

    private fun setUpOrderList() {
        adapter = OrderSheetAdapter { url ->
            FullscreenImageDialogFragment.show(childFragmentManager, url)
        }
        binding.recyclerOrders.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    private fun setUpListeners() = with(binding) {
        buttonBack.setOnClickListener { dismiss() }
        buttonConfirm.setOnClickListener { dismiss() }
        buttonCopy.setOnClickListener { copySheet() }
    }

    private fun observeSheet() {
        viewModel.sheet.observe(viewLifecycleOwner) { sheet ->
            binding.textError.isVisible = sheet == null
            binding.layoutSummary.isVisible = sheet != null
            binding.buttonCopy.isEnabled = sheet != null
            adapter.submitList(sheet?.rows.orEmpty())
            if (sheet != null) renderSummary(sheet)
        }
    }

    private fun renderSummary(sheet: OrderSheet) = with(binding) {
        val room = sheet.room
        textCount.text = getString(R.string.order_sheet_count, room.currentCount, room.maxParticipants)
        textSummary.text = getString(R.string.order_sheet_summary, sheet.rows.size, sheet.submittedCount)
        textDeliveryApp.text = room.deliveryAppLabel(requireContext())
        textFoodTotal.text = getString(R.string.room_detail_won, sheet.foodTotal)
        textDeliveryFee.text = getString(R.string.room_detail_won, sheet.deliveryFee)
        textTotalPayment.text = getString(R.string.room_detail_won, sheet.totalPayment)
    }

    private fun copySheet() {
        val sheet = viewModel.sheet.value ?: return
        val clipboard = requireContext().getSystemService(ClipboardManager::class.java) ?: return
        val clip = ClipData.newPlainText(getString(R.string.order_sheet_title), buildCopyText(sheet))
        clipboard.setPrimaryClip(clip)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(requireContext(), R.string.order_sheet_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildCopyText(sheet: OrderSheet): String = buildString {
        appendLine(getString(R.string.order_sheet_copy_title, sheet.room.title))
        sheet.rows.forEach { row ->
            val name = row.nickname.ifBlank { getString(R.string.chat_sender_participant) }
            val amount = requireContext().orderAmountText(row.amount)
            val line = if (row.isHost) R.string.order_sheet_copy_row_host else R.string.order_sheet_copy_row
            appendLine(getString(line, name, amount))
        }
        append(getString(R.string.order_sheet_copy_total, getString(R.string.room_detail_won, sheet.totalPayment)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "OrderSheetDialog"

        fun show(fragmentManager: FragmentManager, roomId: String) {
            if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(TAG) != null) return
            OrderSheetDialogFragment().apply {
                arguments = bundleOf(OrderSheetViewModel.ARG_ROOM_ID to roomId)
            }.show(fragmentManager, TAG)
        }
    }
}
