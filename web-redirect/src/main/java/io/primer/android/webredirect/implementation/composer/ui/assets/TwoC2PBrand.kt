package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class TwoC2PBrand : Brand {

    override val iconResId: Int
        get() = R.drawable.ic_2c2p_logo
    override val logoResId: Int
        get() = R.drawable.ic_2c2p_logo_square
    override val iconDarkResId: Int
        get() = R.drawable.ic_2c2p_logo_dark
}
