@file:Suppress("unused")

package io.selimdawa.bubblebottom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.abs

internal typealias IBottomNavigationListener = (model: BubbleBottomNavigation.Model) -> Unit

@Suppress("MemberVisibilityCanBePrivate")
class BubbleBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var models = ArrayList<Model>()
    var cells = ArrayList<BubbleBottomNavigationCell>()
        private set
    private var callListenerWhenIsSelected = false

    private var selectedId = -1

    private var onClickedListener: IBottomNavigationListener = {}
    private var onShowListener: IBottomNavigationListener = {}
    private var onReselectListener: IBottomNavigationListener = {}

    private var heightCell = 96.dp(context)
    private var isAnimating = false

    var defaultIconColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var selectedIconColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var backgroundBottomColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var circleColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    private var shadowColor = 0
    var countTextColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var countBackgroundColor = 0
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var countTypeface: Typeface? = null
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    var hasAnimation: Boolean = true
        set(value) {
            field = value
            updateAllIfAllowDraw()
        }
    private var rippleColor = 0

    private var allowDraw = false

    private lateinit var llCells: LinearLayout
    private lateinit var bezierView: BezierView

    init {
        initDefaultColors()
        attrs?.let { setAttributeFromXml(context, it) }
        initializeViews()
    }

    private fun initDefaultColors() {
        defaultIconColor = ContextCompat.getColor(context, R.color.mbn_default_icon_color)
        selectedIconColor = ContextCompat.getColor(context, R.color.mbn_selected_icon_color)
        backgroundBottomColor = ContextCompat.getColor(context, R.color.mbn_background_bottom_color)
        circleColor = ContextCompat.getColor(context, R.color.mbn_circle_color)
        countTextColor = ContextCompat.getColor(context, R.color.mbn_count_text_color)
        countBackgroundColor = ContextCompat.getColor(context, R.color.mbn_count_background_color)
        rippleColor = ContextCompat.getColor(context, R.color.mbn_ripple_color)
        shadowColor = ContextCompat.getColor(context, R.color.mbn_shadow_color)
    }

    private fun setAttributeFromXml(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BubbleBottomNavigation, 0, 0).apply {
            try {
                defaultIconColor = getColor(R.styleable.BubbleBottomNavigation_mbn_defaultIconColor, defaultIconColor)
                selectedIconColor = getColor(R.styleable.BubbleBottomNavigation_mbn_selectedIconColor, selectedIconColor)
                backgroundBottomColor = getColor(R.styleable.BubbleBottomNavigation_mbn_backgroundBottomColor, backgroundBottomColor)
                circleColor = getColor(R.styleable.BubbleBottomNavigation_mbn_circleColor, circleColor)
                countTextColor = getColor(R.styleable.BubbleBottomNavigation_mbn_countTextColor, countTextColor)
                countBackgroundColor = getColor(R.styleable.BubbleBottomNavigation_mbn_countBackgroundColor, countBackgroundColor)
                rippleColor = getColor(R.styleable.BubbleBottomNavigation_mbn_rippleColor, rippleColor)
                shadowColor = getColor(R.styleable.BubbleBottomNavigation_mbn_shadowColor, shadowColor)

                getString(R.styleable.BubbleBottomNavigation_mbn_countTypeface)?.let {
                    if (it.isNotEmpty()) countTypeface = Typeface.createFromAsset(context.assets, it)
                }

                hasAnimation = getBoolean(R.styleable.BubbleBottomNavigation_mbn_hasAnimation, hasAnimation)
            } finally {
                recycle()
            }
        }
    }

    private fun initializeViews() {
        llCells = LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightCell).apply {
                gravity = Gravity.BOTTOM
            }
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
            clipToPadding = false
        }

        bezierView = BezierView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightCell)
            color = backgroundBottomColor
            shadowColor = this@BubbleBottomNavigation.shadowColor
        }

        addView(bezierView)
        addView(llCells)
        allowDraw = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (selectedId == -1) {
            bezierView.bezierX = if (layoutDirection == LayoutDirection.RTL) {
                measuredWidth + 72f.dp(context)
            } else {
                (-72f).dp(context)
            }
        } else {
            show(selectedId, false)
        }
    }

    fun add(model: Model) {
        val cell = BubbleBottomNavigationCell(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, heightCell, 1f)
            icon = model.icon
            count = model.count
            defaultIconColor = this@BubbleBottomNavigation.defaultIconColor
            selectedIconColor = this@BubbleBottomNavigation.selectedIconColor
            circleColor = this@BubbleBottomNavigation.circleColor
            countTextColor = this@BubbleBottomNavigation.countTextColor
            countBackgroundColor = this@BubbleBottomNavigation.countBackgroundColor
            countTypeface = this@BubbleBottomNavigation.countTypeface
            rippleColor = this@BubbleBottomNavigation.rippleColor
            onClickListener = {
                if (isShowing(model.id)) onReselectListener(model)

                if (!isEnabledCell && !isAnimating) {
                    show(model.id, hasAnimation)
                    onClickedListener(model)
                } else if (callListenerWhenIsSelected) {
                    onClickedListener(model)
                }
            }
            disableCell(hasAnimation)
        }

        llCells.addView(cell)
        cells.add(cell)
        models.add(model)
    }

    private fun updateAllIfAllowDraw() {
        if (!allowDraw) return

        cells.forEach {
            it.defaultIconColor = defaultIconColor
            it.selectedIconColor = selectedIconColor
            it.circleColor = circleColor
            it.countTextColor = countTextColor
            it.countBackgroundColor = countBackgroundColor
            it.countTypeface = countTypeface
        }

        bezierView.color = backgroundBottomColor
    }

    private fun anim(cell: BubbleBottomNavigationCell, id: Int, enableAnimation: Boolean = true) {
        isAnimating = true

        val pos = getModelPosition(id)
        val nowPos = getModelPosition(selectedId)
        val dif = abs(pos - (if (nowPos < 0) 0 else nowPos))
        val d = dif * 100L + 150L

        val animDuration = if (enableAnimation && hasAnimation) d else 1L
        val animInterpolator = FastOutSlowInInterpolator()

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animDuration
            interpolator = animInterpolator
            val beforeX = bezierView.bezierX
            addUpdateListener {
                val f = it.animatedFraction
                val newX = cell.x + (cell.measuredWidth / 2)
                bezierView.bezierX = if (newX > beforeX) f * (newX - beforeX) + beforeX
                else beforeX - f * (beforeX - newX)
                if (f == 1f) isAnimating = false
            }
            start()
        }

        if (abs(pos - nowPos) > 1) {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animDuration
                interpolator = animInterpolator
                addUpdateListener { bezierView.progress = it.animatedFraction * 2f }
                start()
            }
        }

        cell.isFromLeft = pos > nowPos
        cells.forEach { it.duration = d }
    }

    fun show(id: Int, enableAnimation: Boolean = true) {
        models.indices.forEach { i ->
            val model = models[i]
            val cell = cells[i]
            if (model.id == id) {
                anim(cell, id, enableAnimation)
                cell.enableCell(enableAnimation)
                onShowListener(model)
            } else {
                cell.disableCell(hasAnimation)
            }
        }
        selectedId = id
    }

    fun isShowing(id: Int) = selectedId == id

    fun getModelById(id: Int) = models.find { it.id == id }

    fun getCellById(id: Int) = cells[getModelPosition(id)]

    fun getModelPosition(id: Int) = models.indexOfFirst { it.id == id }

    fun setCount(id: Int, count: String) {
        val pos = getModelPosition(id)
        if (pos == -1) return
        models[pos].count = count
        cells[pos].count = count
    }

    fun clearCount(id: Int) = setCount(id, BubbleBottomNavigationCell.EMPTY_VALUE)

    fun clearAllCounts() = models.forEach { clearCount(it.id) }

    fun setOnShowListener(listener: IBottomNavigationListener) { onShowListener = listener }

    fun setOnClickMenuListener(listener: IBottomNavigationListener) { onClickedListener = listener }

    fun setOnReselectListener(listener: IBottomNavigationListener) { onReselectListener = listener }

    class Model(var id: Int, var icon: Int) {
        var count: String = BubbleBottomNavigationCell.EMPTY_VALUE
    }
}