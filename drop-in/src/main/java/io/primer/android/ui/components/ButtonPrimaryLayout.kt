package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject

class ButtonPrimaryLayout(ctx: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(ctx, attrs),
    DISdkComponent {

    private val theme: PrimerTheme by inject()

    init {
        background = RippleDrawable(
            ColorStateList(
                arrayOf(
                    IntArray(1) {
                        android.R.attr.state_pressed
                    }
                ),
                IntArray(1) { Color.parseColor("#FFFFFFFF") }
            ),
            GradientDrawable().apply {
                cornerRadius = theme.defaultCornerRadius.getDimension(context)
                color = ColorStateList(
                    arrayOf(
                        IntArray(1) { android.R.attr.state_enabled },
                        IntArray(1) { -android.R.attr.state_enabled }
                    ),
                    IntArray(2) {
                        when (it) {
                            0 -> theme.mainButton.defaultColor.getColor(context, theme.isDarkMode)
                            1 -> theme.mainButton.disabledColor.getColor(context, theme.isDarkMode)
                            else -> theme.mainButton.disabledColor.getColor(
                                context,
                                theme.isDarkMode
                            )
                        }
                    }
                )
                setStroke(
                    1,
                    theme.mainButton.border.defaultColor.getColor(context, theme.isDarkMode)
                )
            },
            null
        )
    }
}
