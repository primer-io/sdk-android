package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import io.primer.android.di.DIAppComponent
import io.primer.android.ui.settings.PrimerTheme
import org.koin.core.component.inject

internal class TextInputWidget(ctx: Context, attrs: AttributeSet? = null) :
    TextInputLayout(ctx, attrs),
    DIAppComponent {

    internal var onValueChanged: (CharSequence?) -> Unit = {}

    private val theme: PrimerTheme by if (isInEditMode) lazy { PrimerTheme.build() } else inject()

    init {
        val colors = intArrayOf(
            theme.input.border.defaultColor.getColor(context, theme.isDarkMode),
            theme.input.border.selectedColor.getColor(context, theme.isDarkMode),
        )

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_focused),
        )

        val colorStateList = ColorStateList(states, colors)

        setBoxStrokeColorStateList(colorStateList)

        val hintColors = intArrayOf(
            theme.input.hintText.defaultColor.getColor(context, theme.isDarkMode),
            theme.input.border.selectedColor.getColor(context, theme.isDarkMode),
        )

        val hintTextStates = arrayOf(
            intArrayOf(-android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_focused),
        )

        hintTextColor = ColorStateList(hintTextStates, hintColors)
        defaultHintTextColor = ColorStateList(hintTextStates, hintColors)

        boxStrokeErrorColor = ColorStateList.valueOf(
            theme.input.border.errorColor.getColor(context, theme.isDarkMode)
        )

        val cornerRadius = theme.input.cornerRadius.getDimension(context)
        setBoxCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)

        boxBackgroundMode = when (theme.inputMode) {
            PrimerTheme.InputMode.OUTLINED -> {
                BOX_BACKGROUND_OUTLINE
            }
            PrimerTheme.InputMode.UNDERLINED -> {
                boxBackgroundColor = theme.input.backgroundColor.getColor(context, theme.isDarkMode)
                BOX_BACKGROUND_FILLED
            }
        }
    }

    internal fun setupEditTextTheme(withTextPrefix: Boolean = false) {
        val fontSize = theme.input.text.fontSize.getDimension(context)
        editText?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            setTextColor(
                theme.input.text.defaultColor.getColor(
                    context,
                    theme.isDarkMode
                )
            )
        }
        if (withTextPrefix) {
            prefixTextView.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                setTextColor(
                    theme.input.text.defaultColor.getColor(
                        context,
                        theme.isDarkMode
                    )
                )
            }
        }
    }

    internal fun setupEditTextListeners() {
        editText?.doAfterTextChanged { text -> onValueChanged(text) }
    }

    internal fun setupEditTextInputFilters(inputCharacters: String?, maxInputLength: Int?) {
        val customFilters = arrayOf(
            inputCharacters?.let {
                object : InputFilter {
                    override fun filter(
                        source: CharSequence?,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int
                    ): CharSequence? {
                        val replacedText = dest?.subSequence(dstart, dend)
                        for (i in start until end) {
                            if (source != null && it.contains(source[i].toString()).not())
                                return replacedText
                        }
                        return null
                    }
                }
            },
            maxInputLength?.let {
                InputFilter.LengthFilter(it)
            }
        ).filterNotNull()

        editText?.apply {
            filters = filters.plus(customFilters)
        }
    }
}
