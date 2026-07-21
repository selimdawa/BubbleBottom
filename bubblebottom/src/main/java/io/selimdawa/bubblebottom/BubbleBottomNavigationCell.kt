package io.selimdawa.bubblebottom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class BubbleBottomNavigationCell @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttrs) {

    private val iv: ImageView
    private val tvCount: TextView
    private val vCircle: View
    private val fl: FrameLayout

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var animationJob: Job? = null
    private var delayJob: Job? = null

    companion object {
        const val EMPTY_VALUE = "empty"
    }

    var defaultIconColor = 0
        set(value) {
            field = value
            updateIconTint()
        }

    var selectedIconColor = 0
        set(value) {
            field = value
            updateIconTint()
        }

    var circleColor = 0
        set(value) {
            field = value
            if (allowDraw) isEnabledCell = isEnabledCell
        }

    var icon = 0
        set(value) {
            field = value
            if (allowDraw && (value != 0)) {
                iv.setImageDrawable(DrawableHelper.changeColorDrawableRes(context, value, -2))
            }
        }

    var count: String? = EMPTY_VALUE
        set(value) {
            field = value
            if (!allowDraw) return

            if (value == EMPTY_VALUE) {
                tvCount.text = ""
                tvCount.visibility = INVISIBLE
            } else {
                val displayCount =
                    if ((value?.length ?: 0) >= 3) value?.substring(0, 1) + ".." else value
                tvCount.apply {
                    text = displayCount
                    visibility = VISIBLE
                    val scale = if (displayCount.isNullOrEmpty()) 0.5f else 1f
                    scaleX = scale
                    scaleY = scale
                }
            }
        }

    private var iconSize = 0f
        set(value) {
            field = value
            if (allowDraw && value > 0f) {
                iv.updateLayoutParams<FrameLayout.LayoutParams> {
                    it.width = value.toInt()
                    it.height = value.toInt()
                }
                iv.pivotX = value / 2f
                iv.pivotY = value / 2f
            }
        }

    var countTextColor = 0
        set(value) {
            field = value
            if (allowDraw) tvCount.setTextColor(field)
        }

    var countBackgroundColor = 0
        set(value) {
            field = value
            if (allowDraw) {
                tvCount.background = GradientDrawable().apply {
                    setColor(field)
                    shape = GradientDrawable.OVAL
                }
            }
        }

    var countTypeface: Typeface? = null
        set(value) {
            field = value
            if (allowDraw && field != null) tvCount.typeface = field
        }

    var rippleColor = 0
        set(value) {
            field = value
            if (allowDraw) isEnabledCell = isEnabledCell
        }

    internal var isFromLeft = false
    internal var duration = 0L
    private var progress = 0f
        set(value) {
            field = value
            if (!allowDraw) return

            fl.y = (1f - progress) * 18f.dp(context) + 13f.dp(context)
            updateIconTint()

            val scale = (1f - progress) * (-0.1f) + 1.1f
            iv.scaleX = scale
            iv.scaleY = scale

            vCircle.background = GradientDrawable().apply {
                setColor(circleColor)
                shape = GradientDrawable.OVAL
            }

            ViewCompat.setElevation(
                vCircle, if (progress > 0.7f) (progress * 4f).dp(context) else 0f
            )

            val m = 24.dp(context)
            vCircle.x =
                (1f - progress) * (if (isFromLeft) -m else m) + ((measuredWidth - 48f.dp(context)) / 2f)
            vCircle.y = (1f - progress) * (measuredHeight - 16.dp(context)) + 22.dp(context)
        }

    var isEnabledCell = false
        set(value) {
            field = value
            if (!allowDraw) return

            val d = GradientDrawable().apply {
                setColor(circleColor)
                shape = GradientDrawable.OVAL
            }
            if (!value) {
                fl.background = RippleDrawable(ColorStateList.valueOf(rippleColor), null, d)
                delayJob?.cancel()
            } else {
                delayJob?.cancel()
                delayJob = scope.launch {
                    delay(200.milliseconds)
                    fl.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

    var onClickListener: () -> Unit = {}
        set(value) {
            field = value
            iv.setOnClickListener { value() }
        }

    private var allowDraw = false

    init {
        LayoutInflater.from(context).inflate(R.layout.bubble_bottom_cell, this, true)
        iv = findViewById(R.id.iv)
        tvCount = findViewById(R.id.tv_count)
        vCircle = findViewById(R.id.v_circle)
        fl = findViewById(R.id.fl)

        iconSize = 48f.dp(context)
        allowDraw = true
        draw()
    }

    private fun draw() {
        if (!allowDraw) return
        if (icon != 0) icon = icon
        count = count
        iconSize = iconSize
        countTextColor = countTextColor
        countBackgroundColor = countBackgroundColor
        countTypeface = countTypeface
        rippleColor = rippleColor
        onClickListener = onClickListener
    }

    private fun updateIconTint() {
        if (allowDraw) {
            val color = if (isEnabledCell) {
                selectedIconColor
            } else {
                ColorHelper.mixTwoColors(selectedIconColor, defaultIconColor, progress)
            }
            ImageViewCompat.setImageTintList(iv, ofColorStateList(color))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        progress = progress
    }

    fun disableCell(isAnimate: Boolean = true) {
        if (isEnabledCell) animateProgress(enableCell = false, isAnimate = isAnimate)
        isEnabledCell = false
    }

    fun enableCell(isAnimate: Boolean = true) {
        if (!isEnabledCell) animateProgress(enableCell = true, isAnimate = isAnimate)
        isEnabledCell = true
    }

    private fun animateProgress(enableCell: Boolean, isAnimate: Boolean = true) {
        animationJob?.cancel()

        if (!isAnimate) {
            progress = if (enableCell) 1f else 0f
            return
        }

        val d = if (enableCell) duration else 250
        val startDelay = if (enableCell) d / 4 else 0L

        animationJob = scope.launch {
            animateValue(d, FastOutSlowInInterpolator(), startDelay) { f ->
                progress = if (enableCell) f else 1f - f
            }
        }
    }

    fun performFlip() {
        iv.animate().rotationYBy(360f).setDuration(400).start()
    }

    fun performZoom() {
        iv.animate().scaleX(1.5f).scaleY(1.5f).setDuration(200).withEndAction {
            iv.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
        }.start()
    }

    fun performRotate() {
        iv.animate().rotationBy(360f).setDuration(400).start()
    }

    fun performShake() {
        iv.animate().translationX(10f).setDuration(50).withEndAction {
            iv.animate().translationX(-10f).setDuration(50).withEndAction {
                iv.animate().translationX(0f).setDuration(50).start()
            }.start()
        }.start()
    }

    fun performFade() {
        iv.animate().alpha(0f).setDuration(200).withEndAction {
            iv.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    fun performTilt() {
        iv.animate().rotation(15f).setDuration(100).withEndAction {
            iv.animate().rotation(-15f).setDuration(200).withEndAction {
                iv.animate().rotation(0f).setDuration(100).start()
            }.start()
        }.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }
}