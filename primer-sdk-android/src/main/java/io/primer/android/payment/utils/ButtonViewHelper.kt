package io.primer.android.payment.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.dPtoPx

internal object ButtonViewHelper {

    private val buttonStates = arrayOf(
        intArrayOf(-android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_selected),
    )

    fun generateButtonContent(context: Context, theme: PrimerTheme): GradientDrawable {
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

    fun generateButtonContent(
        context: Context,
        primerTheme: PrimerTheme,
        displayMetadata: BaseDisplayMetadata
    ): GradientDrawable {
        val contentDrawable = GradientDrawable()
        val border = primerTheme.paymentMethodButton.border
        val borderWidth = displayMetadata.borderWidth?.dPtoPx(context)?.toInt()
        borderWidth?.let {
            contentDrawable.setStroke(
                it,
                displayMetadata.borderColor?.let {
                    ColorStateList.valueOf(Color.parseColor(it))
                } ?: ColorStateList.valueOf(
                    border.defaultColor.getColor(
                        context,
                        primerTheme.isDarkMode
                    )
                )
            )
        }
        displayMetadata.backgroundColor?.let {
            contentDrawable.setColor(Color.parseColor(it))
        }
        val cornerRadiusByTheme = primerTheme.paymentMethodButton.cornerRadius.getDimension(context)
        contentDrawable.cornerRadius = cornerRadiusByTheme
        return contentDrawable
    }
}
