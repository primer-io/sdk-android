package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.utils.ButtonViewHelper.generateButtonContent
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

    private fun render() {
        val content = generateButtonContent(theme, context)
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        background = RippleDrawable(rippleColor, content, null)
    }
}
