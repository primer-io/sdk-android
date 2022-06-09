package io.primer.android.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class TextViewDanger(ctx: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(ctx, attrs),
    DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        setTextColor(theme.errorText.defaultColor.getColor(context, theme.isDarkMode))
    }
}
