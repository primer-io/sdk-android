package io.primer.android.components.assets.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

internal fun Drawable.scaleImage(
    context: Context,
    scaleFactor: Float,
    maxHeight: Float? = null,
): Drawable {
    if (this !is BitmapDrawable) return this

    var newWidth = (intrinsicWidth * scaleFactor).dPtoPx(context)
    var newHeight = (intrinsicHeight * scaleFactor).dPtoPx(context)

    maxHeight?.let {
        if (maxHeight < newHeight) {
            val ratio = maxHeight / newHeight
            newHeight = maxHeight
            newWidth *= ratio
        }
    }

    val scaledBitmap =
        Bitmap.createBitmap(
            newWidth.roundToInt(),
            newHeight.roundToInt(),
            Bitmap.Config.ARGB_8888,
        )

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
        Paint(Paint.FILTER_BITMAP_FLAG),
    )

    return BitmapDrawable(context.resources, scaledBitmap).also {
        bitmap.recycle()
    }
}
