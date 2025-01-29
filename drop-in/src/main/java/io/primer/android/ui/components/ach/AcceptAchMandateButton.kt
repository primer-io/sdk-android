package io.primer.android.ui.components.ach

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.primer.android.databinding.PrimerAcceptAchMandateButtonBinding
import io.primer.android.ui.components.BUTTON_PROGRESS_ALPHA
import io.primer.android.ui.components.FADE_IN_DURATION_MS
import io.primer.android.ui.extensions.setTheme
import io.primer.android.ui.settings.PrimerTheme

internal class AcceptAchMandateButton
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = PrimerAcceptAchMandateButtonBinding.inflate(LayoutInflater.from(context), this, true)

    var text: CharSequence?
        get() = binding.button.text
        set(value) {
            binding.button.text = value
        }

    private var notLoadingText: CharSequence = ""

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

    fun setTheme(theme: PrimerTheme) {
        binding.button.setTheme(theme)
    }
}
