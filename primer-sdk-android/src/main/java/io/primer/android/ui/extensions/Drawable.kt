package io.primer.android.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.utils.dPtoPx
import kotlin.math.roundToInt

internal fun Drawable.updateTint(@ColorInt color: Int): Drawable {
    DrawableCompat.setTint(this, color)
    DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
    return this
}

internal fun Drawable.scaleImage(context: Context, scaleFactor: Float): Drawable {
    if (this !is BitmapDrawable) return this

    val newWidth = (intrinsicWidth * scaleFactor).dPtoPx(context)
    val newHeight = (intrinsicHeight * scaleFactor).dPtoPx(context)
    val scaledBitmap =
        Bitmap.createBitmap(newWidth.roundToInt(), newHeight.roundToInt(), Bitmap.Config.ARGB_8888)

    val ratioX: Float = newWidth / bitmap.width
    val ratioY: Float = newHeight / bitmap.height
    val middleX: Float = newWidth / 2.0f
    val middleY: Float = newHeight / 2.0f

    val scaleMatrix = Matrix()
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

    val canvas = Canvas(scaledBitmap)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(
        bitmap,
        middleX - bitmap.width / 2,
        middleY - bitmap.height / 2,
        Paint(Paint.FILTER_BITMAP_FLAG)
    )

    return BitmapDrawable(context.resources, scaledBitmap).also {
        bitmap.recycle()
    }
}
