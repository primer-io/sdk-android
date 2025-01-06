package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class TrustlyBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_trusly
    override val logoResId: Int
        get() = R.drawable.ic_logo_trustly_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_trustly_light
}
