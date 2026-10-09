package io.selimdawa.bubblebottom

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private var displayDensity = 0f

private fun getDensity(context: Context): Float {
    if (displayDensity == 0f) {
        displayDensity = context.resources.displayMetrics.density
    }
    return displayDensity
}

internal fun Float.dp(context: Context) = this * getDensity(context)
internal fun Int.dp(context: Context) = this * getDensity(context).toInt()

internal suspend fun animateValue(
    duration: Long, interpolator: TimeInterpolator, startDelay: Long = 0L, onUpdate: (Float) -> Unit
) = suspendCancellableCoroutine { continuation ->
    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        this.duration = duration
        this.interpolator = interpolator
        this.startDelay = startDelay
        addUpdateListener { onUpdate(it.animatedFraction) }
        doOnEnd { if (continuation.isActive) continuation.resume(Unit) }
        doOnCancel { if (continuation.isActive) continuation.resume(Unit) }
    }

    continuation.invokeOnCancellation { animator.cancel() }
    animator.start()
}

internal fun ofColorStateList(@ColorInt color: Int): ColorStateList = ColorStateList.valueOf(color)

@Suppress("UNCHECKED_CAST")
fun <T> View?.updateLayoutParams(onLayoutChange: (params: T) -> Unit) {
    this?.let {
        try {
            onLayoutChange(layoutParams as T)
            layoutParams = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}