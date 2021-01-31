package io.primer.android.ui.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonPrimaryLayout(ctx: Context, attrs: AttributeSet? = null) : ConstraintLayout(ctx, attrs), DIAppComponent {
  private val config: CheckoutConfig by inject()

  init {
    background = GradientDrawable().apply {
      setColor(config.theme.buttonPrimaryColor)
      cornerRadius = config.theme.buttonCornerRadius
    }
  }
}