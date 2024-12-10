package io.primer.android.paymentMethods

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class LoadingState(
    @DrawableRes val imageResIs: Int,
    @StringRes val textResId: Int? = null,
    var args: Any? = null
)
