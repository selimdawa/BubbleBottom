package io.selimdawa.bubblebottom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class BezierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0,
) : View(context, attrs, defStyleAttrs) {

    enum class CurveType {
        ROUND, SHARP, SQUARE
    }

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 0f
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mainPath = Path()
    private val shadowPath = Path()
    private val outerArray = FloatArray(22)
    private val innerArray = FloatArray(22)
    private val progressArray = FloatArray(22)

    private var viewWidth = 0f
    private var viewHeight = 0f
    private var bezierOuterWidth = 0f
    private var bezierOuterHeight = 0f
    private var bezierInnerWidth = 0f
    private var bezierInnerHeight = 0f
    private val shadowHeight = 8f.dp(context)

    var verticalOffset = 0f
        set(value) {
            field = value
            updateArrays()
            invalidate()
        }

    var bezierWidthScale = 1f
        set(value) {
            field = value
            updateArrays()
            invalidate()
        }

    var waveAmplitude = 0f
        set(value) {
            field = value
            updateArrays()
            invalidate()
        }

    var glowRadius = 0f
        set(value) {
            field = value
            invalidate()
        }

    var curveType = CurveType.ROUND
        set(value) {
            field = value
            if (viewWidth > 0f) {
                updateDimensions()
                updateArrays()
                invalidate()
            }
        }

    var color = 0
        set(value) {
            field = value
            mainPaint.color = field
            invalidate()
        }

    var shadowColor = 0
        set(value) {
            field = value
            shadowPaint.setShadowLayer(4f.dp(context), 0f, 0f, shadowColor)
            invalidate()
        }

    var bezierX = 0f
        set(value) {
            if (value == field) return
            field = value
            updateArrays()
            invalidate()
        }

    var progress = 0f
        set(value) {
            if (value == field) return
            field = value
            updateArrays()
            invalidate()
        }

    private fun updateArrays() {
        calculateOuter()
        calculateInner()

        // Interpolate BOTH X and Y values for ALL points to ensure a perfectly smooth transition
        for (i in 0..10) {
            val startX = innerArray[i * 2]
            val endX = outerArray[i * 2]
            val startY = innerArray[(i * 2) + 1]
            val endY = outerArray[(i * 2) + 1]

            if (progress <= 1f) {
                progressArray[i * 2] = calculate(startX, endX)
                progressArray[(i * 2) + 1] = calculate(startY, endY)
            } else {
                progressArray[i * 2] = calculate(endX, startX)
                progressArray[(i * 2) + 1] = calculate(endY, startY)
            }
        }
    }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        updateDimensions()
        updateArrays()
    }

    private fun updateDimensions() {
        when (curveType) {
            CurveType.ROUND -> {
                bezierOuterWidth = 72f.dp(context)
                bezierOuterHeight = 24f.dp(context)
                bezierInnerWidth = 124f.dp(context)
                bezierInnerHeight = 32f.dp(context)
            }

            CurveType.SHARP -> {
                bezierOuterWidth = 48f.dp(context)
                bezierOuterHeight = 28f.dp(context)
                bezierInnerWidth = 96f.dp(context)
                bezierInnerHeight = 40f.dp(context)
            }

            CurveType.SQUARE -> {
                bezierOuterWidth = 84f.dp(context)
                bezierOuterHeight = 20f.dp(context)
                bezierInnerWidth = 132f.dp(context)
                bezierInnerHeight = 28f.dp(context)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mainPath.rewind()
        shadowPath.rewind()

        if (progress == 0f || progress >= 2f) {
            drawPath(canvas, isShadow = true, isInner = true)
            drawPath(canvas, isShadow = false, isInner = true)
        } else {
            drawPath(canvas, isShadow = true, isInner = false)
            drawPath(canvas, isShadow = false, isInner = false)
        }

        if (progress >= 2f) progress = 0f
    }

    private fun drawPath(canvas: Canvas, isShadow: Boolean, isInner: Boolean) {
        val paint = if (isShadow) shadowPaint else mainPaint
        val path = if (isShadow) shadowPath else mainPath

        val array = if (isInner) innerArray else progressArray

        path.apply {
            moveTo(array[0], array[1])
            lineTo(array[2], array[3])
            cubicTo(array[4], array[5], array[6], array[7], array[8], array[9])
            cubicTo(array[10], array[11], array[12], array[13], array[14], array[15])
            lineTo(array[16], array[17])
            lineTo(array[18], array[19])
            lineTo(array[20], array[21])
            close()
        }

        canvas.drawPath(path, paint)
    }

    private fun calculateOuter() {
        val extra = shadowHeight + verticalOffset
        val width = bezierOuterWidth * bezierWidthScale
        setPoint(outerArray, 0, 0f, bezierOuterHeight + extra)
        setPoint(outerArray, 1, bezierX - width / 2, bezierOuterHeight + extra)

        val cpOffset = if (curveType == CurveType.SHARP) 8f.dp(context) else width / 4

        setPoint(outerArray, 2, bezierX - cpOffset, bezierOuterHeight + extra + waveAmplitude)
        setPoint(outerArray, 3, bezierX - cpOffset, extra - waveAmplitude)
        setPoint(outerArray, 4, bezierX, extra)
        setPoint(outerArray, 5, bezierX + cpOffset, extra - waveAmplitude)
        setPoint(outerArray, 6, bezierX + cpOffset, bezierOuterHeight + extra + waveAmplitude)
        setPoint(outerArray, 7, bezierX + width / 2, bezierOuterHeight + extra)

        setPoint(outerArray, 8, viewWidth, bezierOuterHeight + extra)
        setPoint(outerArray, 9, viewWidth, viewHeight)
        setPoint(outerArray, 10, 0f, viewHeight)
    }

    private fun calculateInner() {
        val extra = shadowHeight + verticalOffset
        val width = bezierInnerWidth * bezierWidthScale
        val barTop = bezierInnerHeight + extra
        val holeBottom = barTop + 40f.dp(context)

        setPoint(innerArray, 0, 0f, barTop)
        setPoint(innerArray, 1, bezierX - width / 2, barTop)

        val cpOffset = if (curveType == CurveType.SHARP) 12f.dp(context) else width / 4

        setPoint(innerArray, 2, bezierX - cpOffset, barTop + waveAmplitude)
        setPoint(innerArray, 3, bezierX - cpOffset, holeBottom - waveAmplitude)
        setPoint(innerArray, 4, bezierX, holeBottom)
        setPoint(innerArray, 5, bezierX + cpOffset, holeBottom - waveAmplitude)
        setPoint(innerArray, 6, bezierX + cpOffset, barTop + waveAmplitude)
        setPoint(innerArray, 7, bezierX + width / 2, barTop)

        setPoint(innerArray, 8, viewWidth, barTop)
        setPoint(innerArray, 9, viewWidth, viewHeight)
        setPoint(innerArray, 10, 0f, viewHeight)
    }

    private fun setPoint(array: FloatArray, index: Int, x: Float, y: Float) {
        array[index * 2] = x
        array[index * 2 + 1] = y
    }

    private fun calculate(start: Float, end: Float): Float {
        var p = progress
        if (p > 1f) p = progress - 1f
        return (p * (end - start)) + start
    }
}