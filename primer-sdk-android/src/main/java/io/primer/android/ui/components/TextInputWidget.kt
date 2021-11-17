package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class TextInputWidget(ctx: Context, attrs: AttributeSet? = null) :
    TextInputLayout(ctx, attrs),
    DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        val colors = intArrayOf(
            theme.input.border.defaultColor.getColor(context, theme.isDarkMode),
            theme.input.border.selectedColor.getColor(context, theme.isDarkMode),
        )

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_focused),
        )

        val colorStateList = ColorStateList(states, colors)

        setBoxStrokeColorStateList(colorStateList)

        val hintColors = intArrayOf(
            theme.input.hintText.defaultColor.getColor(context, theme.isDarkMode),
            theme.input.border.selectedColor.getColor(context, theme.isDarkMode),
        )

        val hintTextStates = arrayOf(
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_focused),
        )

        hintTextColor = ColorStateList(hintTextStates, hintColors)
        defaultHintTextColor = ColorStateList(hintTextStates, hintColors)

        boxStrokeErrorColor = ColorStateList.valueOf(
            theme.input.border.errorColor.getColor(context, theme.isDarkMode)
        )

        val cornerRadius = theme.input.cornerRadius.getDimension(context)
        setBoxCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)

        boxBackgroundMode = when (theme.inputMode) {
            PrimerTheme.InputMode.OUTLINED -> {
                BOX_BACKGROUND_OUTLINE
            }
            PrimerTheme.InputMode.UNDERLINED -> {
                boxBackgroundColor = theme.input.backgroundColor.getColor(context, theme.isDarkMode)
                BOX_BACKGROUND_FILLED
            }
        }
    }
}
