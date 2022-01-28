package io.primer.android.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.PrimerTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.inject

internal class PrimerTextViewWidget constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatTextView(context, attrs), DIAppComponent {

    private val theme: PrimerTheme by inject()

    init {
        setTextColor(
            theme.titleText.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )
    }
}
