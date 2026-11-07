package com.example.delibuddy.ui.common

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.floor

class PizzaLoadingAnimation(private val pizza: View, private val shadow: View) {
    private var animator: ValueAnimator? = null

    fun start() {
        if (animator != null) return
        val height = 28f * pizza.resources.displayMetrics.density
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val phase = floor((it.animatedValue as Float) * 5f) / 5f
                val lift = 4f * phase * (1f - phase)
                pizza.translationY = -height * lift
                shadow.scaleX = 1f - 0.35f * lift
                shadow.scaleY = 1f - 0.2f * lift
                shadow.alpha = 1f - 0.45f * lift
            }
            start()
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
        pizza.translationY = 0f
        shadow.scaleX = 1f
        shadow.scaleY = 1f
        shadow.alpha = 1f
    }
}
