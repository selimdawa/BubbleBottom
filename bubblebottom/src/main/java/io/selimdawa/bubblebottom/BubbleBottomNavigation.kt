package io.selimdawa.bubblebottom

import android.content.Context
import android.content.ContextWrapper
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

    var animationMode: AnimationMode = AnimationMode.MORPH

    var animationDuration: Long = -1L

    var isBackToHomeEnabled: Boolean = true
        set(value) {
            field = value
            updateBackPressedCallbackState()
        }

    var homeId: Int = -1
        set(value) {
            field = value
            updateBackPressedCallbackState()
        }

    private var initialSelectedId: Int = -1
    private var onBackPressedCallback: OnBackPressedCallback? = null

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
    @Suppress("DiscouragedApi")
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (selectedId != -1) {
            val pos = getModelPosition(selectedId)
            if (pos != -1 && !isAnimating) {
                bezierView.bezierX = getCellCenterX(pos)
            }
        }
    }

    fun getCellCenterX(pos: Int): Float {
        if (pos < 0 || pos >= cells.size) return 0f
        val cell = cells[pos]
        if (cell.width > 0 && (pos == 0 || cell.x > 0)) {
            return cell.x + cell.width / 2f
        }
        val w = if (measuredWidth > 0) measuredWidth.toFloat() else width.toFloat()
        if (w <= 0f || cells.isEmpty()) return 0f
        val cellWidth = w / cells.size
        val effectivePos = if (layoutDirection == LayoutDirection.RTL) cells.size - 1 - pos else pos
        return (effectivePos + 0.5f) * cellWidth
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
        updateBackPressedCallbackState()
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
            getModelPosition = ::getModelPosition,
            getCellCenterX = ::getCellCenterX
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
        if (initialSelectedId == -1 && id != -1) {
            initialSelectedId = id
        }
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
        updateBackPressedCallbackState()
    }

    fun isShowing(id: Int) = selectedId == id

    fun getModelPosition(id: Int) = models.indexOfFirst { it.id == id }

    fun setCount(id: Int, count: String) {
        val pos = getModelPosition(id)
        if (pos == -1) return
        models[pos].count = count
        cells[pos].count = count
    }

    fun setOnShowListener(listener: IBottomNavigationListener) {
        onShowListener = listener
    }

    fun setOnClickMenuListener(listener: IBottomNavigationListener) {
        onClickedListener = listener
    }

    fun setOnReselectListener(listener: IBottomNavigationListener) {
        onReselectListener = listener
    }

    fun getActualHomeId(): Int {
        return when {
            homeId != -1 -> homeId
            initialSelectedId != -1 -> initialSelectedId
            else -> models.firstOrNull()?.id ?: -1
        }
    }

    private fun isNotAtHome(): Boolean {
        val actualHomeId = getActualHomeId()
        return actualHomeId != -1 && selectedId != actualHomeId
    }

    private fun updateBackPressedCallbackState() {
        onBackPressedCallback?.isEnabled = isBackToHomeEnabled && isNotAtHome()
    }

    private fun getDispatcherAndLifecycleOwner(): Pair<OnBackPressedDispatcherOwner, LifecycleOwner>? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is OnBackPressedDispatcherOwner) {
                return Pair(ctx, ctx as LifecycleOwner)
            }
            ctx = ctx.baseContext
        }
        return null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBackNavigation()
    }

    private fun setupBackNavigation() {
        if (onBackPressedCallback != null) {
            updateBackPressedCallbackState()
            return
        }
        val owners = getDispatcherAndLifecycleOwner() ?: return
        val dispatcherOwner = owners.first
        val lifecycleOwner = owners.second

        val callback = object : OnBackPressedCallback(isBackToHomeEnabled && isNotAtHome()) {
            override fun handleOnBackPressed() {
                val actualHomeId = getActualHomeId()
                if (actualHomeId != -1 && selectedId != actualHomeId) {
                    show(actualHomeId)
                }
            }
        }
        dispatcherOwner.onBackPressedDispatcher.addCallback(lifecycleOwner, callback)
        onBackPressedCallback = callback
        updateBackPressedCallbackState()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
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
        updateBackPressedCallbackState()
    }
}