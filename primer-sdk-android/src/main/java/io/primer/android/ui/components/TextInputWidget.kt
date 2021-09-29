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
            theme.input.text.defaultColor.getColor(context),
            theme.input.border.defaultColor.getColor(context),
        )

        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_pressed),
        )

        val colorStateList = ColorStateList(states, colors)

        setBoxStrokeColorStateList(colorStateList)
        hintTextColor = colorStateList

        val hintTextStates = arrayOf(
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_focused),
        )

        defaultHintTextColor = ColorStateList(hintTextStates, colors)

        boxStrokeErrorColor = ColorStateList.valueOf(
            theme.input.border.errorColor.getColor(context)
        )

        val cornerRadius = theme.input.cornerRadius.getDimension(context)
        setBoxCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)

        boxBackgroundMode = when (theme.inputMode) {
            PrimerTheme.InputMode.OUTLINED -> {
                BOX_BACKGROUND_OUTLINE
            }
            PrimerTheme.InputMode.UNDERLINED -> {
                boxBackgroundColor = theme.input.backgroundColor.getColor(context)
                BOX_BACKGROUND_FILLED
            }
        }
    }
}
