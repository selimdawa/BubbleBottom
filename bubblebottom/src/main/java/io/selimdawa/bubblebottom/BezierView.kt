package io.selimdawa.bubblebottom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class BezierView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0
) : View(context, attrs, defStyleAttrs) {

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
        progressArray[2] = bezierX - bezierInnerWidth / 2
        progressArray[4] = bezierX - bezierInnerWidth / 4
        progressArray[6] = bezierX - bezierInnerWidth / 4
        progressArray[8] = bezierX
        progressArray[10] = bezierX + bezierInnerWidth / 4
        progressArray[12] = bezierX + bezierInnerWidth / 4
        progressArray[14] = bezierX + bezierInnerWidth / 2

        progressArray[0] = outerArray[0]
        progressArray[16] = outerArray[16]
        progressArray[18] = outerArray[18]
        progressArray[20] = outerArray[20]

        for (i in 0..10) {
            if (i in 2..6) {
                progressArray[i * 2 + 1] = if (progress <= 1f) {
                    calculate(innerArray[i * 2 + 1], outerArray[i * 2 + 1])
                } else {
                    calculate(outerArray[i * 2 + 1], innerArray[i * 2 + 1])
                }
            } else {
                progressArray[i * 2 + 1] = if (progress <= 1f) {
                    innerArray[i * 2 + 1]
                } else {
                    outerArray[i * 2 + 1]
                }
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
        bezierOuterWidth = 72f.dp(context)
        bezierOuterHeight = 8f.dp(context)
        bezierInnerWidth = 124f.dp(context)
        bezierInnerHeight = 16f.dp(context)
        updateArrays()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mainPath.rewind()
        shadowPath.rewind()

        if (progress == 0f || progress >= 2f) {
            drawPath(canvas, true, isInner = true)
            drawPath(canvas, false, isInner = true)
        } else {
            drawPath(canvas, true, isInner = false)
            drawPath(canvas, false, isInner = false)
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
            cubicTo(
                array[10], array[11], array[12], array[13], array[14], array[15]
            )
            lineTo(array[16], array[17])
            lineTo(array[18], array[19])
            lineTo(array[20], array[21])
            close()
        }

        canvas.drawPath(path, paint)
    }

    private fun calculateOuter() {
        val extra = shadowHeight
        setPoint(outerArray, 0, 0f, bezierOuterHeight + extra)
        setPoint(outerArray, 1, bezierX - bezierOuterWidth / 2, bezierOuterHeight + extra)
        setPoint(outerArray, 2, bezierX - bezierOuterWidth / 4, bezierOuterHeight + extra)
        setPoint(outerArray, 3, bezierX - bezierOuterWidth / 4, extra)
        setPoint(outerArray, 4, bezierX, extra)
        setPoint(outerArray, 5, bezierX + bezierOuterWidth / 4, extra)
        setPoint(outerArray, 6, bezierX + bezierOuterWidth / 4, bezierOuterHeight + extra)
        setPoint(outerArray, 7, bezierX + bezierOuterWidth / 2, bezierOuterHeight + extra)
        setPoint(outerArray, 8, viewWidth, bezierOuterHeight + extra)
        setPoint(outerArray, 9, viewWidth, viewHeight)
        setPoint(outerArray, 10, 0f, viewHeight)
    }

    private fun calculateInner() {
        val extra = shadowHeight
        setPoint(innerArray, 0, 0f, bezierInnerHeight + extra)
        setPoint(innerArray, 1, bezierX - bezierInnerWidth / 2, bezierInnerHeight + extra)
        setPoint(innerArray, 2, bezierX - bezierInnerWidth / 4, bezierInnerHeight + extra)
        setPoint(innerArray, 3, bezierX - bezierInnerWidth / 4, viewHeight - extra)
        setPoint(innerArray, 4, bezierX, viewHeight - extra)
        setPoint(innerArray, 5, bezierX + bezierInnerWidth / 4, viewHeight - extra)
        setPoint(innerArray, 6, bezierX + bezierInnerWidth / 4, bezierInnerHeight + extra)
        setPoint(innerArray, 7, bezierX + bezierInnerWidth / 2, bezierInnerHeight + extra)
        setPoint(innerArray, 8, viewWidth, bezierInnerHeight + extra)
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