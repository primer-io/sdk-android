package io.primer.android.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.primer.android.R
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.ui.PayAmountText

private const val FADE_IN_DURATION_MS = 900L
private const val FADE_OUT_DURATION_MS = 300L

class PayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val button: Button
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

    override fun isEnabled(): Boolean = button.isEnabled

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
            .alpha(1f)
            .setDuration(FADE_IN_DURATION_MS)
            .start()
    }

    fun hideProgress() {
        progressIndicator
            .animate()
            .alpha(1f)
            .setDuration(FADE_OUT_DURATION_MS)
            .withEndAction { progressIndicator.isVisible = false }
            .start()
        button.text = notLoadingText
        button.isEnabled = true
    }

    // copied over from ButtonPrimary, should be reworked alongside UniversalCheckoutTheme
    fun setTheme(theme: UniversalCheckoutTheme) {
        val buttonColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_pressed)),
            intArrayOf(Color.parseColor("#FFFFFFFF")),
        )
        val content = GradientDrawable().apply {
            cornerRadius = theme.buttonCornerRadius
            color = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled)
                ),
                intArrayOf(
                    theme.buttonPrimaryColor,
                    theme.buttonPrimaryColorDisabled,
                    theme.buttonPrimaryColor
                )
            )
            setStroke(1, theme.buttonDefaultBorderColor)
        }
        button.background = RippleDrawable(buttonColor, content, null)
    }
}
