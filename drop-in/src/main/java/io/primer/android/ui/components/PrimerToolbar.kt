package io.primer.android.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import io.primer.android.R
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.settings.PrimerTheme

internal class PrimerToolbar
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), DISdkComponent {
    private val backButton: ImageView
    private val closeButton: ImageView
    private val title: TextView
    private val logo: ImageView

    private val theme: PrimerTheme by inject()

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.primer_layout_toolbar, this, true)

        backButton = findViewById(R.id.toolbar_back)
        closeButton = findViewById(R.id.toolbar_close)
        title = findViewById(R.id.toolbar_title)
        logo = findViewById(R.id.toolbar_logo)

        ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(
                context,
                theme.isDarkMode,
            ),
        ).run {
            backButton.imageTintList = this
            closeButton.imageTintList = this
        }

        title.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, theme.titleText.fontSize.getDimension(context))
            setTextColor(theme.titleText.defaultColor.getColor(context, theme.isDarkMode))
        }
    }

    fun getBackButton() = backButton.apply { isVisible = true }

    fun getCloseButton() = closeButton.apply { isVisible = true }

    fun showOnlyTitle(
        @StringRes text: Int,
    ) {
        logo.apply { isVisible = false }
        title.apply {
            setText(text)
            isVisible = true
        }
    }

    fun showOnlyLogo(
        @DrawableRes image: Int,
    ) {
        title.apply { isVisible = false }
        logo.apply {
            setImageResource(image)
            isVisible = true
        }
    }
}
