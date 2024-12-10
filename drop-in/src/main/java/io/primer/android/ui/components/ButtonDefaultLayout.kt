package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.payment.utils.ButtonViewHelper.generateButtonContent

class ButtonDefaultLayout(
    ctx: Context,
    attrs: AttributeSet? = null
) : LinearLayout(ctx, attrs), DISdkComponent {

    private val theme: PrimerTheme by inject()

    init {
        render()
    }

    private fun render() {
        val content = generateButtonContent(context, theme)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        background = RippleDrawable(rippleColor, content, null)
    }
}
