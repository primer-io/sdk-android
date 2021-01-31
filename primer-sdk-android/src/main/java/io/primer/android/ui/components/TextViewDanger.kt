package io.primer.android.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class TextViewDanger(ctx: Context, attrs: AttributeSet? = null) : AppCompatTextView(ctx, attrs), DIAppComponent {
  private val config: CheckoutConfig by inject()

  init {
    setTextColor(config.theme.textDangerColor)
  }
}