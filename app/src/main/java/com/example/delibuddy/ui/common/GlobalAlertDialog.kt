package com.example.delibuddy.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.delibuddy.R
import com.example.delibuddy.databinding.DialogGlobalAlertBinding

class GlobalAlertDialog : DialogFragment() {

    private var _binding: DialogGlobalAlertBinding? = null
    private val binding get() = _binding!!

    enum class Type {
        CONFIRM,
        DANGER,
        INFO,
    }

    private val alertType: Type
        get() {
            val typeName = requireArguments().getString(ARG_TYPE)
            return Type.entries.firstOrNull { it.name == typeName } ?: Type.CONFIRM
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val theme = when (alertType) {
            Type.CONFIRM -> R.style.GlobalAlertDialogTheme
            Type.DANGER -> R.style.GlobalAlertDialogDangerTheme
            Type.INFO -> R.style.GlobalAlertDialogInfoTheme
        }
        val context = ContextThemeWrapper(inflater.context, theme)
        _binding = DialogGlobalAlertBinding.inflate(inflater.cloneInContext(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpContent()
        setUpListeners()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpContent() {
        val args = requireArguments()
        val description = args.getString(ARG_DESCRIPTION)

        binding.textTitle.text = args.getString(ARG_TITLE)
        binding.textDescription.text = description
        binding.textDescription.isVisible = !description.isNullOrBlank()
        binding.buttonCancel.isVisible = alertType != Type.INFO
        binding.buttonConfirm.text = args.getString(ARG_CONFIRM_LABEL)
            ?: getString(R.string.alert_confirm)
    }

    private fun setUpListeners() {
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonConfirm.setOnClickListener {
            binding.buttonConfirm.isEnabled = false
            val requestKey = requireArguments().getString(ARG_REQUEST_KEY)
            val manager = parentFragmentManager
            dismiss()
            if (requestKey != null) {
                manager.setFragmentResult(requestKey, requireArguments().getBundle(ARG_RESULT) ?: Bundle.EMPTY)
            }
        }
    }

    companion object {
        private const val TAG = "GlobalAlertDialog"
        private const val ARG_TYPE = "type"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_CONFIRM_LABEL = "confirm_label"
        private const val ARG_REQUEST_KEY = "request_key"
        private const val ARG_RESULT = "result"

        fun show(
            fragmentManager: FragmentManager,
            type: Type,
            title: String,
            description: String? = null,
            confirmLabel: String? = null,
            requestKey: String? = null,
            result: Bundle? = null,
        ) {
            if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(TAG) != null) return
            GlobalAlertDialog().apply {
                arguments = bundleOf(
                    ARG_TYPE to type.name,
                    ARG_TITLE to title,
                    ARG_DESCRIPTION to description,
                    ARG_CONFIRM_LABEL to confirmLabel,
                    ARG_REQUEST_KEY to requestKey,
                    ARG_RESULT to result,
                )
            }.showNow(fragmentManager, TAG)
        }
    }
}
