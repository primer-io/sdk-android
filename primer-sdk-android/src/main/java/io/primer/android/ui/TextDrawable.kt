package io.primer.android.ui

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint

import android.util.TypedValue

import android.graphics.Paint.Align

import android.graphics.drawable.Drawable
import android.graphics.RectF
import android.util.TypedValue.COMPLEX_UNIT_SP
import io.primer.android.R

internal class TextDrawable(private val res: Resources, private val text: String) : Drawable() {

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mPaint.color = DEFAULT_COLOR
        mPaint.textAlign = Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        drawCircle(canvas)
        val count = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

        // draw text
        val width: Int = bounds.width()
        val height: Int = bounds.height()
        val fontSize =
            TypedValue.applyDimension(COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, res.displayMetrics)
        mPaint.textSize = fontSize
        canvas.drawText(
            text,
            (width / 2).toFloat(),
            height / 2 - (mPaint.descent() + mPaint.ascent()) / 2,
            mPaint
        )

        canvas.restoreToCount(count)
    }

    private fun drawCircle(canvas: Canvas) {
        val rect = RectF(bounds)
        val borderPaint = Paint()
        borderPaint.color = res.getColor(R.color.primer_bank_selection_placeholder_color)
        canvas.drawCircle(rect.centerX(), rect.centerY(), rect.height() / 2, borderPaint)
    }

    override fun getOpacity(): Int {
        return mPaint.alpha
    }

    override fun getIntrinsicWidth(): Int {
        return -1
    }

    override fun getIntrinsicHeight(): Int {
        return -1
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        mPaint.colorFilter = filter
    }

    companion object {
        private const val DEFAULT_COLOR: Int = Color.BLACK
        private const val DEFAULT_TEXT_SIZE = 16f
    }
}
