package io.primer.android.ui.extensions

import android.content.res.ColorStateList
import com.google.android.material.button.MaterialButton
import io.primer.android.ui.settings.PrimerTheme

fun MaterialButton.setTheme(theme: PrimerTheme) {
    val enabledStates = intArrayOf(android.R.attr.state_enabled)
    val disabledStates = intArrayOf(-android.R.attr.state_enabled)
    val states = arrayOf(enabledStates, disabledStates)
    val enabledColor = theme.mainButton.defaultColor.getColor(context, theme.isDarkMode)
    val disabledColor = theme.mainButton.disabledColor.getColor(context, theme.isDarkMode)
    val colors = intArrayOf(enabledColor, disabledColor)
    val strokeColor = theme.mainButton.border.defaultColor.getColor(context, theme.isDarkMode)
    cornerRadius = theme.mainButton.cornerRadius.getPixels(context)
    strokeWidth = theme.mainButton.border.width.getPixels(context)
    this.strokeColor = ColorStateList.valueOf(strokeColor)
    backgroundTintList = ColorStateList(states, colors)
}
