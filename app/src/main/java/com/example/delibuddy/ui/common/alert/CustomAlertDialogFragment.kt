package com.example.delibuddy.ui.common.alert

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.delibuddy.R

enum class AlertIconType {
    CONFIRM,
    DANGER,
    INFO
}

class CustomAlertDialogFragment : DialogFragment() {

    var onConfirmListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_custom_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageIcon = view.findViewById<ImageView>(R.id.image_icon)
        val textTitle = view.findViewById<TextView>(R.id.text_title)
        val textDescription = view.findViewById<TextView>(R.id.text_description)
        val buttonCancel = view.findViewById<Button>(R.id.button_cancel)
        val buttonConfirm = view.findViewById<Button>(R.id.button_confirm)

        val typeName = arguments?.getString(ARG_TYPE) ?: AlertIconType.CONFIRM.name
        val type = runCatching { AlertIconType.valueOf(typeName) }.getOrDefault(AlertIconType.CONFIRM)
        val title = arguments?.getString(ARG_TITLE).orEmpty()
        val desc = arguments?.getString(ARG_DESCRIPTION).orEmpty()

        textTitle.text = title
        textDescription.text = desc
        textDescription.isVisible = desc.isNotBlank()

        when (type) {
            AlertIconType.CONFIRM -> {
                imageIcon.setImageResource(R.drawable.question_mark_icon)
                buttonCancel.isVisible = true
                buttonConfirm.text = "확인"
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F49A36"))
                buttonConfirm.setTextColor(Color.parseColor("#212130"))
            }
            AlertIconType.DANGER -> {
                imageIcon.setImageResource(R.drawable.x_mark_icon)
                buttonCancel.isVisible = true
                buttonConfirm.text = "내보내기"
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D62B38"))
                buttonConfirm.setTextColor(Color.WHITE)
            }
            AlertIconType.INFO -> {
                imageIcon.setImageResource(R.drawable.question_mark_icon)
                buttonCancel.isVisible = false
                buttonConfirm.text = "확인"
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F49A36"))
                buttonConfirm.setTextColor(Color.parseColor("#212130"))
            }
        }

        buttonCancel.setOnClickListener { dismiss() }
        buttonConfirm.setOnClickListener {
            dismiss()
            onConfirmListener?.invoke()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    companion object {
        const val TAG = "CustomAlertDialog"
        private const val ARG_TYPE = "arg_type"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"

        fun show(
            fragmentManager: FragmentManager,
            type: AlertIconType,
            title: String,
            description: String,
            onConfirm: () -> Unit
        ) {
            if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(TAG) != null) return
            CustomAlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                }
                this.onConfirmListener = onConfirm
            }.showNow(fragmentManager, TAG)
        }
    }
}
