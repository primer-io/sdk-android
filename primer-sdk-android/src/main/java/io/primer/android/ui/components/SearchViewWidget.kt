package io.primer.android.ui.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import io.primer.android.R
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.ui.extensions.setCompoundDrawablesWithIntrinsicBoundsTinted
import io.primer.android.ui.settings.PrimerTheme

internal class SearchViewWidget(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), DISdkComponent {

    private val theme: PrimerTheme by inject()

    init {
        setHintTextColor(
            theme.searchInput.hintText.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )
        setTextColor(theme.searchInput.text.defaultColor.getColor(context, theme.isDarkMode))

        doAfterTextChanged {
            val drawableResId = when (it.isNullOrBlank()) {
                true -> R.drawable.ic_search
                else -> R.drawable.ic_search_clear
            }
            setCompoundDrawablesWithIntrinsicBoundsTinted(
                0,
                0,
                drawableResId,
                0,
                theme.searchInput.text.defaultColor.getColor(context, theme.isDarkMode)
            )
        }

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (right - compoundDrawables[DRAWABLE_END_INDEX].bounds.width())) {
                    setText("")
                    true
                }
            }
            false
        }

        background = GradientDrawable().apply {
            cornerRadius = theme.searchInput.cornerRadius.getDimension(context)
            setStroke(
                resources.getDimensionPixelSize(R.dimen.primer_bank_search_input_border_width),
                ContextCompat.getColor(this@SearchViewWidget.context, R.color.primer_bank_search_input_border)
            )
            setColor(
                theme.searchInput.backgroundColor.getColor(
                    context = context,
                    isDarkMode = theme.isDarkMode
                )
            )
        }

        maxLines = 1
        inputType = EditorInfo.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_SEARCH
    }

    private companion object {
        const val DRAWABLE_END_INDEX = 2
    }
}
