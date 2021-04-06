package io.primer.android.ui.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
// FIXME rename to PrimerNestedScrollView
class NestedScrollViewSurface(context: Context, attrs: AttributeSet? = null) :
    NestedScrollView(context, attrs), DIAppComponent {

    private val theme: UniversalCheckoutTheme by inject()

    init {
        background = ColorDrawable(theme.backgroundColor)
    }
}
