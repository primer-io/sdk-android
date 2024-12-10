package io.primer.android.googlepay.implementation.composer.ui.assets

import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.assets.ui.model.Brand
import io.primer.android.googlepay.R

internal class GooglePayBrand(private val buttonStyle: GooglePayButtonStyle) : Brand {

    override val iconResId: Int
        get() = R.drawable.ic_logo_googlepay

    override val logoResId: Int
        get() = when (buttonStyle) {
            GooglePayButtonStyle.BLACK ->
                R.drawable.ic_logo_google_pay_black_square

            GooglePayButtonStyle.WHITE ->
                R.drawable.ic_logo_google_pay_square
        }

    override val iconLightResId: Int
        get() = R.drawable.ic_logo_googlepay_light
}
