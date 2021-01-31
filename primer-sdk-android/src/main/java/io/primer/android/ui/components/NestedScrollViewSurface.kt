package io.primer.android.ui.components

import android.content.Context
import android.graphics.BlendModeColorFilter
import android.graphics.ColorFilter
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.NestedScrollView
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class NestedScrollViewSurface(context: Context, attrs: AttributeSet? = null) : NestedScrollView(context, attrs), DIAppComponent {
  private val theme: UniversalCheckoutTheme by inject()

  init {
    background = ColorDrawable(theme.backgroundColor)
  }
}