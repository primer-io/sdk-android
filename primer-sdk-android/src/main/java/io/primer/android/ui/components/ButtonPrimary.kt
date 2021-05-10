package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import io.primer.android.R
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.di.DIAppComponent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class ButtonPrimary(ctx: Context, attrs: AttributeSet) :
    LinearLayout(ctx, attrs),
    DIAppComponent {

    private val mText: TextView
    private val mProgress: ProgressBar
    private val theme: UniversalCheckoutTheme by inject()

    var text: CharSequence
        get() = mText.text
        set(value) {
            mText.text = value
        }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(R.layout.layout_button_primary, this, true)

        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        background = createBackground()
        mText = findViewById(R.id.button_primary_cta_text)
        mProgress = findViewById(R.id.button_primary_cta_progress)
        text = attrs.getAttributeValue(R.styleable.ButtonPrimary_buttonText)

        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
        )

        requestLayout()
    }

    fun setProgress(active: Boolean) {
        mProgress.visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun createBackground(): Drawable {
        return RippleDrawable(
            ColorStateList(
                arrayOf(
                    IntArray(1) {
                        android.R.attr.state_pressed
                    }
                ),
                // FIXME default value should come from resources
                IntArray(1) { Color.parseColor("#FFFFFFFF") },
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
                            0 -> theme.buttonPrimaryColor
                            1 -> theme.buttonPrimaryColorDisabled
                            else -> theme.buttonPrimaryColor
                        }
                    }
                )
                setStroke(1, theme.buttonDefaultBorderColor)
            },
            null
        )
    }
}
