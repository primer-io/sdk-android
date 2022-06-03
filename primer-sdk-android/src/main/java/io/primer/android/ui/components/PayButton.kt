package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.databinding.PayButtonBinding
import io.primer.android.model.MonetaryAmount
import io.primer.android.ui.PayAmountText

private const val FADE_IN_DURATION_MS = 900L

internal class PayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = PayButtonBinding.inflate(LayoutInflater.from(context), this, true)

    var text: CharSequence?
        get() = binding.button.text
        set(value) {
            binding.button.text = value
        }

    init {
        text = resources.getString(R.string.pay)
    }

    var amount: MonetaryAmount? = null
        set(value) {
            field = value
            text = if (value == null) {
                resources.getString(R.string.pay)
            } else {
                val amountValue = PayAmountText.generate(context, value)
                resources.getString(R.string.pay_specific_amount, amountValue)
            }
        }

    private var notLoadingText = text

    private val isLoading
        get() = !binding.button.isEnabled && binding.progressIndicator.isVisible

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.button.setOnClickListener(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        binding.button.isEnabled = enabled
    }

    override fun isEnabled(): Boolean =
        binding.button.isEnabled

    @SuppressLint("ClickableViewAccessibility")
    override fun performClick(): Boolean =
        if (isLoading) {
            binding.button.performClick()
        } else {
            binding.progressIndicator.performClick()
        }

    fun showProgress() {
        notLoadingText = binding.button.text
        binding.button.text = ""
        binding.button.isEnabled = false
        binding.progressIndicator.isVisible = true
        binding.progressIndicator
            .animate()
            .alpha(0.5f)
            .setDuration(FADE_IN_DURATION_MS)
            .start()
    }

    fun setTheme(theme: PrimerTheme) {
        val enabledStates = intArrayOf(android.R.attr.state_enabled)
        val disabledStates = intArrayOf(-android.R.attr.state_enabled)
        val states = arrayOf(enabledStates, disabledStates)
        val enabledColor = theme.mainButton.defaultColor.getColor(context, theme.isDarkMode)
        val disabledColor = theme.mainButton.disabledColor.getColor(context, theme.isDarkMode)
        val colors = intArrayOf(enabledColor, disabledColor)
        val strokeColor = theme.mainButton.border.defaultColor.getColor(context, theme.isDarkMode)
        binding.button.cornerRadius = theme.mainButton.cornerRadius.getPixels(context)
        binding.button.strokeWidth = theme.mainButton.border.width.getPixels(context)
        binding.button.strokeColor = ColorStateList.valueOf(strokeColor)
        binding.button.backgroundTintList = ColorStateList(states, colors)
    }
}
