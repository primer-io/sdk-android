package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class PayNowBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_xfers
    override val logoResId: Int
        get() = R.drawable.ic_logo_xfers_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_xfers_light
}
