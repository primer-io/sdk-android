package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import io.primer.android.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonDefaultLayout(
    ctx: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(ctx, attrs), DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        render()
    }

    private fun generateButtonContent(context: Context): GradientDrawable {
        val content = GradientDrawable()
        val strokeColor =
            theme.paymentMethodButton.border.defaultColor.getColor(context, theme.isDarkMode)
        val width = theme.paymentMethodButton.border.width.getPixels(context)
        content.setStroke(width, strokeColor)
        content.cornerRadius = theme.paymentMethodButton.cornerRadius.getDimension(context)
        return content
    }

    private fun render() {
        val content = generateButtonContent(context)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        background = RippleDrawable(rippleColor, content, null)
    }
}
