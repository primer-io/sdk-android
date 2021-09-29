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
import android.widget.ProgressBar
import android.widget.TextView
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonPrimary(
    ctx: Context,
    attrs: AttributeSet,
) : LinearLayout(ctx, attrs), DIAppComponent {

    private val textView: TextView
    private val progressBar: ProgressBar
    private val theme: PrimerTheme by inject()

    var text: CharSequence
        get() = textView.text
        set(value) {
            textView.text = value
        }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(R.layout.layout_button_primary, this, true)

        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        background = createBackground()
        textView = findViewById(R.id.button_primary_cta_text)
        progressBar = findViewById(R.id.button_primary_cta_progress)
        text = attrs.getAttributeValue(R.styleable.ButtonPrimary_buttonText)

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        requestLayout()
    }

    fun setProgress(active: Boolean) {
        progressBar.visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun createBackground(): Drawable {
        val splash = theme.splashColor.getColor(context)
        val rippleColor = ColorStateList.valueOf(splash)
        val content = GradientDrawable().apply {
            cornerRadius = theme.defaultCornerRadius.getDimension(context)
            color = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled)
                ),
                intArrayOf(
                    theme.mainButton.defaultColor.getColor(context),
                    theme.mainButton.disabledColor.getColor(context),
                )
            )
        }
        return RippleDrawable(rippleColor, content, null)
    }
}
