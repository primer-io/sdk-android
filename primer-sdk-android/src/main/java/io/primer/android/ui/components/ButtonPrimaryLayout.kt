package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import io.primer.android.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonPrimaryLayout(ctx: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(ctx, attrs),
    DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        background = RippleDrawable(
            ColorStateList(
                arrayOf(
                    IntArray(1) {
                        android.R.attr.state_pressed
                    }
                ),
                IntArray(1) { Color.parseColor("#FFFFFFFF") },
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
                            0 -> theme.mainButton.defaultColor.getColor(context)
                            1 -> theme.mainButton.disabledColor.getColor(context)
                            else -> theme.mainButton.disabledColor.getColor(context)
                        }
                    }
                )
                setStroke(1, theme.mainButton.border.defaultColor.getColor(context))
            },
            null
        )
    }
}
