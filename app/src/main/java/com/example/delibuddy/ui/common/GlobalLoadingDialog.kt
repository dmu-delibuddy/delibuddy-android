package com.example.delibuddy.ui.common

import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.delibuddy.R
import com.example.delibuddy.databinding.DialogGlobalLoadingBinding

class GlobalLoadingDialog : DialogFragment() {
    private var binding: DialogGlobalLoadingBinding? = null
    private var bounce: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = DialogGlobalLoadingBinding.inflate(LayoutInflater.from(requireContext()))
        binding = content
        content.loadingMessage.text = arguments?.getString(MESSAGE)
            ?: getString(R.string.global_loading_message)
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(content.root)
            setCanceledOnTouchOutside(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.6f)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        val content = binding ?: return
        val height = 28f * resources.displayMetrics.density
        bounce = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val phase = it.animatedValue as Float
                val lift = 4f * phase * (1f - phase)
                content.loadingPizza.translationY = -height * lift
                content.loadingShadow.scaleX = 1f - 0.35f * lift
                content.loadingShadow.scaleY = 1f - 0.2f * lift
                content.loadingShadow.alpha = 1f - 0.45f * lift
            }
            start()
        }
    }

    override fun onStop() {
        bounce?.cancel()
        bounce = null
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "global_loading"
        private const val MESSAGE = "message"

        fun show(manager: FragmentManager, message: String? = null) {
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            GlobalLoadingDialog().apply {
                arguments = bundleOf(MESSAGE to message)
            }.showNow(manager, TAG)
        }

        fun hide(manager: FragmentManager) {
            (manager.findFragmentByTag(TAG) as? GlobalLoadingDialog)
                ?.dismissAllowingStateLoss()
        }
    }
}
