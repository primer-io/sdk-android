package io.primer.android.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.settings.PrimerTheme

class TextViewDanger(ctx: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(ctx, attrs),
    DISdkComponent {
    private val theme: PrimerTheme by inject()

    init {
        setTextColor(theme.errorText.defaultColor.getColor(context, theme.isDarkMode))
    }
}
