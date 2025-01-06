package io.primer.android.paypal.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.paypal.R

internal class PaypalBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_paypal

    override val logoResId: Int
        get() = R.drawable.ic_logo_paypal_square
}
