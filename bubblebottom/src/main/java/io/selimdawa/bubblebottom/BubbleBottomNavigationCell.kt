package io.selimdawa.bubblebottom

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.selimdawa.bubblebottom.databinding.BubbleBottomCellBinding

@Suppress("unused")
class BubbleBottomNavigationCell @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0
) : RelativeLayout(context, attrs, defStyleAttrs) {

    private val binding: BubbleBottomCellBinding =
        BubbleBottomCellBinding.inflate(LayoutInflater.from(context), this)

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
            if (allowDraw) binding.iv.setImageResource(value)
        }

    var count: String? = EMPTY_VALUE
        set(value) {
            field = value
            if (!allowDraw) return
            
            if (value == EMPTY_VALUE) {
                binding.tvCount.text = ""
                binding.tvCount.visibility = INVISIBLE
            } else {
                val displayCount = if ((value?.length ?: 0) >= 3) value?.substring(0, 1) + ".." else value
                binding.tvCount.apply {
                    text = displayCount
                    visibility = VISIBLE
                    val scale = if (displayCount.isNullOrEmpty()) 0.5f else 1f
                    scaleX = scale
                    scaleY = scale
                }
            }
        }

    private var iconSize = 48f.dp(context)
        set(value) {
            field = value
            if (allowDraw) {
                binding.iv.updateLayoutParams<FrameLayout.LayoutParams> {
                    it.width = value.toInt()
                    it.height = value.toInt()
                }
                binding.iv.pivotX = iconSize / 2f
                binding.iv.pivotY = iconSize / 2f
            }
        }

    var countTextColor = 0
        set(value) {
            field = value
            if (allowDraw) binding.tvCount.setTextColor(field)
        }

    var countBackgroundColor = 0
        set(value) {
            field = value
            if (allowDraw) {
                binding.tvCount.background = GradientDrawable().apply {
                    setColor(field)
                    shape = GradientDrawable.OVAL
                }
            }
        }

    var countTypeface: Typeface? = null
        set(value) {
            field = value
            if (allowDraw && field != null) binding.tvCount.typeface = field
        }

    var rippleColor = 0
        set(value) {
            field = value
            if (allowDraw) isEnabledCell = isEnabledCell
        }

    var isFromLeft = false
    var duration = 0L
    private var progress = 0f
        set(value) {
            field = value
            if (!allowDraw) return

            binding.fl.y = (1f - progress) * 18f.dp(context) - 3f.dp(context)
            updateIconTint()
            
            val scale = (1f - progress) * (-0.1f) + 1.1f
            binding.iv.scaleX = scale
            binding.iv.scaleY = scale

            binding.vCircle.background = GradientDrawable().apply {
                setColor(circleColor)
                shape = GradientDrawable.OVAL
            }

            ViewCompat.setElevation(binding.vCircle, if (progress > 0.7f) (progress * 4f).dp(context) else 0f)

            val m = 24.dp(context)
            binding.vCircle.x = (1f - progress) * (if (isFromLeft) -m else m) + ((measuredWidth - 48f.dp(context)) / 2f)
            binding.vCircle.y = (1f - progress) * measuredHeight + 6.dp(context)
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
                binding.fl.background = RippleDrawable(ColorStateList.valueOf(rippleColor), null, d)
            } else {
                binding.fl.runAfterDelay(200) {
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

    var onClickListener: () -> Unit = {}
        set(value) {
            field = value
            binding.iv.setOnClickListener { value() }
        }

    private var allowDraw = false

    init {
        allowDraw = true
        draw()
    }

    private fun draw() {
        if (!allowDraw) return
        icon = icon
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
            ImageViewCompat.setImageTintList(
                binding.iv,
                ofColorStateList(if (progress == 1f || isEnabledCell) selectedIconColor else defaultIconColor)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        progress = progress
    }

    fun disableCell(isAnimate: Boolean = true) {
        if (isEnabledCell) animateProgress(false, isAnimate)
        isEnabledCell = false
    }

    fun enableCell(isAnimate: Boolean = true) {
        if (!isEnabledCell) animateProgress(true, isAnimate)
        isEnabledCell = true
    }

    private fun animateProgress(enableCell: Boolean, isAnimate: Boolean = true) {
        val d = if (enableCell) duration else 250
        ValueAnimator.ofFloat(0f, 1f).apply {
            startDelay = if (enableCell) d / 4 else 0L
            duration = if (isAnimate) d else 1L
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val f = it.animatedFraction
                progress = if (enableCell) f else 1f - f
            }
            start()
        }
    }
}
