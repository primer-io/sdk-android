package io.primer.android.payment.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import io.primer.android.ui.settings.PrimerTheme

internal object ButtonViewHelper {

    private val buttonStates = arrayOf(
        intArrayOf(-android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_selected),
    )

    fun generateButtonContent(theme: PrimerTheme, context: Context): GradientDrawable {
        val contentDrawable = GradientDrawable()
        val border = theme.paymentMethodButton.border
        val unSelectedColor = border.defaultColor.getColor(context, theme.isDarkMode)
        val selectedColor = border.selectedColor.getColor(context, theme.isDarkMode)
        val colors = intArrayOf(unSelectedColor, selectedColor)
        val borderStates = ColorStateList(buttonStates, colors)
        val width = border.width.getPixels(context)
        contentDrawable.setStroke(width, borderStates)
        val background = theme.paymentMethodButton.defaultColor.getColor(context, theme.isDarkMode)
        contentDrawable.setColor(background)
        contentDrawable.cornerRadius = theme.paymentMethodButton.cornerRadius.getDimension(context)
        return contentDrawable
    }
}
