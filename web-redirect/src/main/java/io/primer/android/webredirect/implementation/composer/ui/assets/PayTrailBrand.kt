package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class PayTrailBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_paytrail
    override val logoResId: Int
        get() = R.drawable.ic_logo_paytrail_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_paytrail_light
}
