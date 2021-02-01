package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonDefaultLayout(ctx: Context, attrs: AttributeSet? = null): LinearLayout(ctx, attrs), DIAppComponent {
  private val theme: UniversalCheckoutTheme by inject()

  init {
    background = RippleDrawable(
      ColorStateList(
        arrayOf(IntArray(0)),
        IntArray(1) { theme.buttonDefaultBorderColor },
      ),
      GradientDrawable().apply {
        cornerRadius = theme.buttonCornerRadius
        color = ColorStateList(
          arrayOf(
            IntArray(1) { android.R.attr.state_enabled },
            IntArray(1) { -android.R.attr.state_enabled }
          ),
          IntArray(2) {
            when (it) {
              0 -> theme.buttonDefaultColor
              1 -> theme.buttonDefaultColorDisabled
              else -> theme.buttonDefaultColor
            }
          }
        )
        setStroke(1, theme.buttonDefaultBorderColor)
      },
      null
    )
  }
}