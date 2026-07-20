package io.selimdawa.bubblebottom

import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

class BubbleBottomNavigationAnimator(
    private val scope: CoroutineScope,
    private val bezierView: BezierView,
    private val cells: List<BubbleBottomNavigationCell>
) {
    private var animationJob: Job? = null
    private var secondAnimationJob: Job? = null
    private var effectJob: Job? = null

    fun cancelAll() {
        animationJob?.cancel()
        secondAnimationJob?.cancel()
        effectJob?.cancel()
    }

    fun animate(
        cell: BubbleBottomNavigationCell,
        id: Int,
        selectedId: Int,
        mode: AnimationMode,
        duration: Long,
        hasAnimation: Boolean,
        getModelPosition: (Int) -> Int
    ) {
        cancelAll()

        val pos = getModelPosition(id)
        val nowPos = getModelPosition(selectedId)
        val dif = abs(pos - (if (nowPos < 0) 0 else nowPos))
        val d = if (duration != -1L) duration else dif * 100L + 150L

        val animDuration = if (hasAnimation) d else 1L

        val interpolator = when (mode) {
            AnimationMode.BOUNCE -> BounceInterpolator()
            AnimationMode.ANTICIPATE -> AnticipateInterpolator()
            AnimationMode.ELASTIC -> OvershootInterpolator(2f)
            AnimationMode.FLING -> android.view.animation.AccelerateInterpolator()
            else -> FastOutSlowInInterpolator()
        }

        val beforeX = bezierView.bezierX
        val targetX = cell.x + (cell.measuredWidth / 2)

        // Reset effects
        bezierView.verticalOffset = 0f
        bezierView.bezierWidthScale = 1f
        bezierView.waveAmplitude = 0f

        animationJob = scope.launch {
            animateValue(animDuration, interpolator) { f ->
                bezierView.bezierX = if (targetX > beforeX) f * (targetX - beforeX) + beforeX
                else beforeX - f * (beforeX - targetX)

                applyModeEffects(mode, f)
            }
            // Finalize some effects
            bezierView.verticalOffset = 0f
            bezierView.bezierWidthScale = 1f
            bezierView.waveAmplitude = 0f
        }

        if ((mode == AnimationMode.MORPH || mode == AnimationMode.JELLY) && abs(pos - nowPos) > 1) {
            secondAnimationJob = scope.launch {
                animateValue(animDuration, interpolator) { f ->
                    bezierView.progress = f * 2f
                }
            }
        }

        triggerCellAnimations(cell, mode)
    }

    private fun applyModeEffects(mode: AnimationMode, f: Float) {
        when (mode) {
            AnimationMode.DROP -> {
                bezierView.verticalOffset = (1f - f) * (-50f)
            }

            AnimationMode.JUMP -> {
                bezierView.verticalOffset = sin(f * Math.PI).toFloat() * (-100f)
            }

            AnimationMode.SQUASH -> {
                bezierView.bezierWidthScale = 1f - sin(f * Math.PI).toFloat() * 0.5f
            }

            AnimationMode.WAVE -> {
                bezierView.waveAmplitude = sin(f * Math.PI * 2).toFloat() * 20f
            }

            AnimationMode.JELLY -> {
                bezierView.bezierWidthScale = 1f + sin(f * Math.PI * 4).toFloat() * 0.2f * (1f - f)
            }

            AnimationMode.GLOW -> {
                bezierView.glowRadius = sin(f * Math.PI).toFloat() * 20f
            }

            else -> {}
        }
    }

    private fun triggerCellAnimations(cell: BubbleBottomNavigationCell, mode: AnimationMode) {
        when (mode) {
            AnimationMode.FLIP -> cell.performFlip()
            AnimationMode.ZOOM -> cell.performZoom()
            AnimationMode.ROTATE -> cell.performRotate()
            AnimationMode.SHAKE -> cell.performShake()
            AnimationMode.FADE -> cell.performFade()
            AnimationMode.TILT -> cell.performTilt()
            AnimationMode.SPIN_MOVE -> cell.performRotate()
            AnimationMode.PULSE -> cell.performZoom()
            else -> {}
        }
    }
}