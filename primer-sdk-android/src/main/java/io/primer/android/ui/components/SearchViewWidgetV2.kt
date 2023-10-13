package io.primer.android.ui.components

import android.annotation.SuppressLint
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
import io.primer.android.ui.extensions.NO_COLOR
import io.primer.android.ui.extensions.setCompoundDrawablesWithIntrinsicBoundsTinted
import io.primer.android.ui.settings.PrimerTheme

@SuppressLint("ClickableViewAccessibility")
internal class SearchViewWidgetV2(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), DISdkComponent {

    private val theme: PrimerTheme by
    if (isInEditMode) {
        lazy { PrimerTheme.build() }
    } else { inject() }

    init {
        setHintTextColor(
            theme.searchInput.hintText.defaultColor.getColor(
                context,
                theme.isDarkMode
            )
        )
        setTextColor(theme.searchInput.text.defaultColor.getColor(context, theme.isDarkMode))

        ContextCompat.getDrawable(context, R.drawable.ic_search_gray)?.let { searchIconDrawable ->
            setCompoundDrawablesWithIntrinsicBounds(
                searchIconDrawable,
                null,
                null,
                null
            )
        }

        doAfterTextChanged {
            setCompoundDrawablesWithIntrinsicBoundsTinted(
                R.drawable.ic_search_gray,
                0,
                if (it.isNullOrBlank()) 0 else R.drawable.ic_search_clear,
                0,
                theme.searchInput.text.defaultColor.getColor(context, theme.isDarkMode),
                tintColorLeft = NO_COLOR
            )
        }

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && compoundDrawables.isNotEmpty()) {
                val iconBounds = compoundDrawables[DRAWABLE_END_INDEX]?.bounds?.width()
                if (iconBounds != null && event.rawX >= (right - iconBounds)) {
                    setText("")
                    true
                }
            }
            false
        }

        val roundedBackground = GradientDrawable()
        roundedBackground.cornerRadius = theme.searchInput.cornerRadius.getDimension(context)
        roundedBackground.setColor(
            theme.searchInput.backgroundColor.getColor(
                context,
                theme.isDarkMode
            )
        )
        background = roundedBackground

        maxLines = 1
        inputType = EditorInfo.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_SEARCH
    }

    private companion object {
        const val DRAWABLE_END_INDEX = 2
    }
}
