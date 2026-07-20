@file:Suppress("unused")

package io.selimdawa.bubblebottom

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
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

internal object DrawableHelper {

    fun changeColorDrawableVector(c: Context?, resDrawable: Int, color: Int): Drawable? {
        val context = c ?: return null
        return VectorDrawableCompat.create(context.resources, resDrawable, null)?.mutate()?.apply {
            if (color != -2) DrawableCompat.setTint(this, color)
        }
    }

    fun changeColorDrawableRes(c: Context?, resDrawable: Int, color: Int): Drawable? {
        val context = c ?: return null
        return ContextCompat.getDrawable(context, resDrawable)?.mutate()?.apply {
            if (color != -2) DrawableCompat.setTint(this, color)
        }
    }
}

internal object ColorHelper {

    fun mixTwoColors(color1: Int, color2: Int, amount: Float): Int {
        val inverseAmount = 1.0f - amount

        val a =
            ((color1 shr 24 and 0xff) * amount + (color2 shr 24 and 0xff) * inverseAmount).toInt() and 0xff
        val r =
            ((color1 shr 16 and 0xff) * amount + (color2 shr 16 and 0xff) * inverseAmount).toInt() and 0xff
        val g =
            ((color1 shr 8 and 0xff) * amount + (color2 shr 8 and 0xff) * inverseAmount).toInt() and 0xff
        val b = ((color1 and 0xff) * amount + (color2 and 0xff) * inverseAmount).toInt() and 0xff

        return a shl 24 or (r shl 16) or (g shl 8) or b
    }
}

internal fun Context.getDrawableCompat(res: Int) = ContextCompat.getDrawable(this, res)

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