package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.ui.PayAmountText

private const val FADE_IN_DURATION_MS = 900L
private const val FADE_OUT_DURATION_MS = 300L

class PayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val button: MaterialButton
    private val progressIndicator: CircularProgressIndicator

    var text: CharSequence?
        get() = button.text
        set(value) {
            button.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.pay_button, this, true)
        button = findViewById(R.id.button)
        progressIndicator = findViewById(R.id.progressIndicator)
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
        get() = !button.isEnabled && progressIndicator.isVisible

    override fun setOnClickListener(listener: OnClickListener?) {
        button.setOnClickListener(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        button.isEnabled = enabled
    }

    override fun isEnabled(): Boolean =
        button.isEnabled

    @SuppressLint("ClickableViewAccessibility")
    override fun performClick(): Boolean =
        if (isLoading) {
            button.performClick()
        } else {
            progressIndicator.performClick()
        }

    fun showProgress() {
        notLoadingText = button.text
        button.text = ""
        button.isEnabled = false
        progressIndicator.isVisible = true
        progressIndicator
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
        button.cornerRadius = theme.mainButton.cornerRadius.getPixels(context)
        button.strokeWidth = theme.mainButton.border.width.getPixels(context)
        button.strokeColor = ColorStateList.valueOf(strokeColor)
        button.backgroundTintList = ColorStateList(states, colors)
    }
}
