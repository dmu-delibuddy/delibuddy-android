package com.example.delibuddy.ui.room.create

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.delibuddy.R
import com.example.delibuddy.databinding.LayoutCreateRoomBottomBinding
import com.example.delibuddy.databinding.LayoutCreateRoomHeaderBinding
import com.example.delibuddy.ui.common.GlobalAlertDialog

private const val REQUEST_EXIT = "exit_create"

fun Fragment.setUpCreateRoomHeader(header: LayoutCreateRoomHeaderBinding, step: Int) {
    header.textStep.text = "$step/${CreateRoomViewModel.LAST_STEP}"
    listOf(header.progress1, header.progress2, header.progress3, header.progress4)
        .forEachIndexed { i, bar ->
            bar.setBackgroundResource(
                if (i < step) R.drawable.bg_progress_on else R.drawable.bg_progress_off
            )
        }

    childFragmentManager.setFragmentResultListener(REQUEST_EXIT, viewLifecycleOwner) { _, _ ->
        findNavController().popBackStack(R.id.navigation_create_room, true)
    }
    header.buttonBack.setOnClickListener {
        GlobalAlertDialog.show(
            fragmentManager = childFragmentManager,
            type = GlobalAlertDialog.Type.DANGER,
            title = getString(R.string.room_create_exit_title),
            description = getString(R.string.room_create_exit_description),
            confirmLabel = getString(R.string.room_create_exit_confirm),
            requestKey = REQUEST_EXIT,
        )
    }
}

fun Fragment.setUpCreateRoomBottom(
    bottom: LayoutCreateRoomBottomBinding,
    step: Int,
    onNext: () -> Unit,
) {
    bottom.buttonPrev.isVisible = step > 1
    bottom.buttonPrev.setOnClickListener { findNavController().navigateUp() }
    bottom.buttonNext.setText(
        if (step == CreateRoomViewModel.LAST_STEP) R.string.room_create_submit
        else R.string.room_create_next
    )
    bottom.buttonNext.setOnClickListener { onNext() }
}
