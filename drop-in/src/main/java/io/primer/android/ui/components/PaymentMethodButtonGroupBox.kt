package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import io.primer.android.R
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.settings.PrimerTheme

class PaymentMethodButtonGroupBox
@JvmOverloads
constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle), DISdkComponent {
    private val theme: PrimerTheme by inject()

    private val label = TextView(context)

    init {
        render()
        label.textAlignment = TEXT_ALIGNMENT_VIEW_END
        label.gravity = Gravity.END
        addView(label, 0)
    }

    private fun render() {
        orientation = VERTICAL
        val backgroundColor =
            ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(context, R.color.primer_secondary),
                    BACKGROUND_ALPHA,
                ),
            )
        background =
            GradientDrawable().apply {
                color = backgroundColor
                cornerRadius = context.resources.getDimension(R.dimen.primer_default_corner_radius)
            }
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams = params
        setPadding(context.resources.getDimension(R.dimen.medium_padding).toInt())
        setMargin()
    }

    fun setMargin(
        topMargin: Int = 16,
        bottomMargin: Int = 16,
    ) {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, topMargin, 0, bottomMargin)
        layoutParams = params
    }

    fun hideSurchargeFrame(
        padding: Int = 0,
        topMargin: Int = 24,
    ) {
        setPadding(padding, 0, padding, 0)
        val newBackground = GradientDrawable()
        newBackground.color =
            ColorStateList.valueOf(theme.backgroundColor.getColor(context, theme.isDarkMode))
        setMargin(topMargin = topMargin, bottomMargin = 0)
        this.background = newBackground
        label.isVisible = false
    }

    fun showSurchargeLabel(
        text: String,
        isBold: Boolean = false,
    ) {
        label.text = text
        if (isBold) label.typeface = Typeface.DEFAULT_BOLD
        val textColor =
            ColorStateList.valueOf(
                theme.amountLabelText.defaultColor.getColor(context, theme.isDarkMode),
            )
        label.setTextColor(textColor)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(
            0,
            0,
            0,
            context.resources.getDimension(R.dimen.medium_vertical_margin2).toInt(),
        )
        label.layoutParams = params
        label.isVisible = true
    }

    private companion object {
        const val BACKGROUND_ALPHA = 12
    }
}
