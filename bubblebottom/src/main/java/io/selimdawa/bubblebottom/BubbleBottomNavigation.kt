@file:Suppress("unused")

package io.selimdawa.bubblebottom

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class BubbleBottomNavigation @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var animator: BubbleBottomNavigationAnimator

    private val _selectedIdFlow = MutableStateFlow(-1)

    val selectedIdFlow: StateFlow<Int> = _selectedIdFlow.asStateFlow()

    var animationMode: AnimationMode = AnimationMode.MORPH

    var animationDuration: Long = -1L

    var isBackToHomeEnabled: Boolean = true
    var homeId: Int = -1

    var curveType: BezierView.CurveType = BezierView.CurveType.ROUND
        set(value) {
            field = value
            if (allowDraw) bezierView.curveType = value
        }

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
        defaultIconColor = resolveColorByName(
            "colorOnSurfaceVariant", ContextCompat.getColor(context, R.color.mbn_default_icon_color)
        )
        selectedIconColor = resolveColorByName(
            "colorPrimary", ContextCompat.getColor(context, R.color.mbn_selected_icon_color)
        )
        backgroundBottomColor = resolveColorByName(
            "colorSurface", ContextCompat.getColor(context, R.color.mbn_background_bottom_color)
        )
        circleColor = resolveColorByName(
            "colorPrimaryContainer", ContextCompat.getColor(context, R.color.mbn_circle_color)
        )
        countTextColor = resolveColorByName(
            "onSecondaryContainer", ContextCompat.getColor(context, R.color.mbn_count_text_color)
        )
        countBackgroundColor = resolveColorByName(
            "secondaryContainer",
            ContextCompat.getColor(context, R.color.mbn_count_background_color)
        )
        rippleColor = resolveColorByName(
            "colorControlHighlight", ContextCompat.getColor(context, R.color.mbn_ripple_color)
        )
        shadowColor = ContextCompat.getColor(context, R.color.mbn_shadow_color)
    }

    @ColorInt
    private fun resolveColorByName(name: String, @ColorInt fallback: Int): Int {
        val attrId = context.resources.getIdentifier(name, "attr", context.packageName)
        return if (attrId != 0) {
            MaterialColors.getColor(context, attrId, fallback)
        } else {
            // Try with common library package names if needed, or just use appcompat default
            val appCompatId = context.resources.getIdentifier(name, "attr", "androidx.appcompat")
            if (appCompatId != 0) {
                MaterialColors.getColor(context, appCompatId, fallback)
            } else {
                fallback
            }
        }
    }

    private fun setAttributeFromXml(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BubbleBottomNavigation, 0, 0)
            .apply {
                try {
                    defaultIconColor = getColor(
                        R.styleable.BubbleBottomNavigation_mbn_defaultIconColor, defaultIconColor
                    )
                    selectedIconColor = getColor(
                        R.styleable.BubbleBottomNavigation_mbn_selectedIconColor, selectedIconColor
                    )
                    backgroundBottomColor = getColor(
                        R.styleable.BubbleBottomNavigation_mbn_backgroundBottomColor,
                        backgroundBottomColor
                    )
                    circleColor =
                        getColor(R.styleable.BubbleBottomNavigation_mbn_circleColor, circleColor)
                    countTextColor = getColor(
                        R.styleable.BubbleBottomNavigation_mbn_countTextColor, countTextColor
                    )
                    countBackgroundColor = getColor(
                        R.styleable.BubbleBottomNavigation_mbn_countBackgroundColor,
                        countBackgroundColor
                    )
                    rippleColor =
                        getColor(R.styleable.BubbleBottomNavigation_mbn_rippleColor, rippleColor)
                    shadowColor =
                        getColor(R.styleable.BubbleBottomNavigation_mbn_shadowColor, shadowColor)

                    getString(R.styleable.BubbleBottomNavigation_mbn_countTypeface)?.let {
                        if (it.isNotEmpty()) countTypeface =
                            Typeface.createFromAsset(context.assets, it)
                    }

                    hasAnimation = getBoolean(
                        R.styleable.BubbleBottomNavigation_mbn_hasAnimation, hasAnimation
                    )
                    isHapticFeedbackEnabled = getBoolean(
                        R.styleable.BubbleBottomNavigation_mbn_isHapticFeedbackEnabled,
                        isHapticFeedbackEnabled
                    )
                    val curveValue = getInt(R.styleable.BubbleBottomNavigation_mbn_curveType, 0)
                    curveType = BezierView.CurveType.entries[curveValue]

                    val animModeValue =
                        getInt(R.styleable.BubbleBottomNavigation_mbn_animationMode, 1)
                    animationMode = AnimationMode.entries[animModeValue]

                    animationDuration = getInt(
                        R.styleable.BubbleBottomNavigation_mbn_animationDuration, -1
                    ).toLong()

                    isBackToHomeEnabled = getBoolean(
                        R.styleable.BubbleBottomNavigation_mbn_backToHomeEnabled, true
                    )
                    homeId = getInt(R.styleable.BubbleBottomNavigation_mbn_homeId, -1)
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

        animator = BubbleBottomNavigationAnimator(scope, bezierView, cells)

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
                    if (isHapticFeedbackEnabled) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
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
        animator.animate(
            cell = cell,
            id = id,
            selectedId = selectedId,
            mode = animationMode,
            duration = animationDuration,
            hasAnimation = enableAnimation && hasAnimation,
            getModelPosition = ::getModelPosition
        )
        isAnimating = false
        cell.isFromLeft = getModelPosition(id) > getModelPosition(selectedId)
        cells.forEach {
            it.duration = if (animationDuration != -1L) animationDuration else abs(
                getModelPosition(id) - getModelPosition(selectedId)
            ) * 100L + 150L
        }
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
        _selectedIdFlow.value = id
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

    fun setOnShowListener(listener: IBottomNavigationListener) {
        onShowListener = listener
    }

    fun setOnClickMenuListener(listener: IBottomNavigationListener) {
        onClickedListener = listener
    }

    fun setOnReselectListener(listener: IBottomNavigationListener) {
        onReselectListener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBackNavigation()
    }

    private fun setupBackNavigation() {
        if (!isBackToHomeEnabled) return
        val dispatcherOwner = context as? OnBackPressedDispatcherOwner ?: return
        val lifecycleOwner = context as? LifecycleOwner ?: return

        dispatcherOwner.onBackPressedDispatcher.addCallback(
            lifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val actualHomeId = if (homeId != -1) homeId else models.firstOrNull()?.id ?: -1
                    if (selectedId != actualHomeId && actualHomeId != -1) {
                        show(actualHomeId)
                    } else {
                        isEnabled = false
                        dispatcherOwner.onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("selectedId", selectedId)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state
        if (state is Bundle) {
            selectedId = state.getInt("selectedId", -1)
            superState = BundleCompat.getParcelable(state, "superState", Parcelable::class.java)
        }
        super.onRestoreInstanceState(superState)
    }
}