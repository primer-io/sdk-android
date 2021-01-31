package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonDefaultLayout(ctx: Context, attrs: AttributeSet? = null): LinearLayout(ctx, attrs), DIAppComponent {
  private val config: CheckoutConfig by inject()

  init {
    background = GradientDrawable().apply {
      cornerRadius = config.theme.buttonCornerRadius
      setColor(config.theme.buttonDefaultColor)
    }
  }
}