package io.primer.android.banks.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.banks.R

internal class DotpayBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_dotpay_dark
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_dotpay_light
}
