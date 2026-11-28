package com.example.delibuddy.ui.signup

import com.example.delibuddy.R
import com.example.delibuddy.databinding.LayoutSignupProgressBinding

fun LayoutSignupProgressBinding.setStep(step: Int) {
    listOf(progress1, progress2, progress3, progress4).forEachIndexed { i, bar ->
        bar.setBackgroundResource(
            if (i < step) R.drawable.bg_progress_on else R.drawable.bg_progress_off
        )
    }
}
