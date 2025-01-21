package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.settings.PrimerTheme

class PrimerThemedLinearLayout(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs), DISdkComponent {
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
