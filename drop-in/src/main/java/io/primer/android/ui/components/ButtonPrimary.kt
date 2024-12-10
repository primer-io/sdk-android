package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import io.primer.android.R
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.databinding.LayoutButtonPrimaryBinding

internal class ButtonPrimary(
    ctx: Context,
    attrs: AttributeSet
) : LinearLayout(ctx, attrs), DISdkComponent {

    private val binding = LayoutButtonPrimaryBinding.inflate(LayoutInflater.from(context), this)
    private val theme: PrimerTheme by inject()

    var text: CharSequence
        get() = binding.buttonPrimaryCtaText.text
        set(value) {
            binding.buttonPrimaryCtaText.text = value
        }

    init {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        background = createBackground()
        text = attrs.getAttributeValue(R.styleable.ButtonPrimary_buttonText)

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        requestLayout()
    }

    fun setProgress(active: Boolean) {
        binding.buttonPrimaryCtaProgress.visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun createBackground(): Drawable {
        val splash = theme.splashColor.getColor(context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        val content = GradientDrawable().apply {
            cornerRadius = theme.defaultCornerRadius.getDimension(context)
            color = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled)
                ),
                intArrayOf(
                    theme.mainButton.defaultColor.getColor(context, theme.isDarkMode),
                    theme.mainButton.disabledColor.getColor(context, theme.isDarkMode)
                )
            )
        }
        return RippleDrawable(rippleColor, content, null)
    }
}
