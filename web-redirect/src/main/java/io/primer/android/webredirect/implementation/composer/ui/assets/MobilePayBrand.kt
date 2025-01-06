package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class MobilePayBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_mobilepay
    override val logoResId: Int
        get() = R.drawable.ic_logo_mobilepay_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_mobilepay_light
}
