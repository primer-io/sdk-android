package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class HoolahBrand : Brand {

    override val iconResId: Int
        get() = R.drawable.ic_logo_hoolah
    override val logoResId: Int
        get() = R.drawable.ic_logo_hoolah_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_hoolah_light
}
