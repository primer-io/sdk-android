package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.primer.android.R
import io.primer.android.databinding.PrimerPayButtonBinding
import io.primer.android.ui.settings.PrimerTheme

internal class PayButton
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = PrimerPayButtonBinding.inflate(LayoutInflater.from(context), this, true)

    var text: CharSequence?
        get() = binding.button.text
        set(value) {
            binding.button.text = value
        }

    init {
        text = resources.getString(R.string.pay)
    }

    var amount: String? = null
        set(value) {
            field = value
            text =
                if (value == null) {
                    resources.getString(R.string.pay)
                } else {
                    resources.getString(R.string.pay_specific_amount, amount)
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

    override fun isEnabled(): Boolean = binding.button.isEnabled

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
            .alpha(BUTTON_PROGRESS_ALPHA)
            .setDuration(FADE_IN_DURATION_MS)
            .start()
    }

    fun hideProgress() {
        binding.button.text = notLoadingText
        binding.button.isEnabled = true
        binding.progressIndicator.isVisible = false
        binding.progressIndicator.clearAnimation()
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
