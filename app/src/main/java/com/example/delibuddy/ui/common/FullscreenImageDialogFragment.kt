package com.example.delibuddy.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.delibuddy.databinding.DialogFullscreenImageBinding

class FullscreenImageDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = DialogFullscreenImageBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogFullscreenImageBinding.bind(view)
        binding.root.loadImage(requireArguments().getString(ARG_IMAGE).orEmpty())
        binding.root.setOnClickListener { dismiss() }
    }

    companion object {
        private const val ARG_IMAGE = "image"
        private const val TAG = "FullscreenImageDialog"

        fun show(fragmentManager: FragmentManager, image: String) {
            if (fragmentManager.isStateSaved || fragmentManager.findFragmentByTag(TAG) != null) return
            FullscreenImageDialogFragment().apply {
                arguments = bundleOf(ARG_IMAGE to image)
            }.showNow(fragmentManager, TAG)
        }
    }
}
