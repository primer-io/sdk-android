package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding

class PaymentMethodButtonGroupBox @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val label = TextView(context)

    init {
        render()
        label.textAlignment = TEXT_ALIGNMENT_VIEW_END
        label.gravity = Gravity.END
        addView(label, 0)
    }

    private fun render() {
        orientation = VERTICAL
        val backgroundColor = ColorStateList.valueOf(Color.argb(12, 0, 0, 0))
        background = GradientDrawable().apply {
            color = backgroundColor
            cornerRadius = 16f
        }
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams = params
        setPadding(24)
        setTopMargin()
    }

    fun setTopMargin(margin: Int = 24) {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, margin, 0, 0)
        layoutParams = params
    }

    fun hideSurchargeFrame() {
        setPadding(0)
        val newBackground = GradientDrawable()
        newBackground.color = ColorStateList.valueOf(Color.WHITE)
        this.background = newBackground
        label.isVisible = false
    }

    fun showSurchargeLabel(text: String, isBold: Boolean = false) {
        label.text = text
        if (isBold) label.typeface = Typeface.DEFAULT_BOLD
        val textColor = ColorStateList.valueOf(Color.BLACK)
        label.setTextColor(textColor)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 16)
        label.layoutParams = params
        label.isVisible = true
    }
}
