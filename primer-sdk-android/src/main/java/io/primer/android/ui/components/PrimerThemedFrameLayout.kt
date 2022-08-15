package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.inject
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout

class PrimerThemedFrameLayout(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        val shape = GradientDrawable()
        val r = theme.bottomSheetCornerRadius.getDimension(context)
        shape.cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
        shape.color =
            ColorStateList.valueOf(theme.backgroundColor.getColor(context, theme.isDarkMode))
        background = shape
    }
}
