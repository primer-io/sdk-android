package io.primer.android.ui.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class TextInputWidget(ctx: Context, attrs:AttributeSet? = null) : TextInputLayout(ctx, attrs), DIAppComponent {
  private val config: CheckoutConfig by inject()

  init {
    background = GradientDrawable().apply {
      setColor(config.theme.inputBackgroundColor)
      cornerRadius = config.theme.inputCornerRadius
    }
  }
}