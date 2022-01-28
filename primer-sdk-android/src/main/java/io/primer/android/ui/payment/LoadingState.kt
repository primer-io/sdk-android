package io.primer.android.ui.payment

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class LoadingState(
    @DrawableRes val imageResIs: Int,
    @StringRes val textResId: Int? = null
)
