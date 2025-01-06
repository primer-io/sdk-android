package io.primer.android.ui.utils

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import io.primer.android.R
import io.primer.android.ui.components.TextInputWidget

internal fun TextInputWidget.setMarginBottomForError(isErrorState: Boolean) {
    val marginBottom =
        resources.getDimensionPixelSize(
            if (isErrorState) {
                R.dimen.primer_input_spacing_error_vert
            } else {
                R.dimen.primer_input_spacing_vert
            },
        )
    this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.bottomMargin = marginBottom
    }
}
