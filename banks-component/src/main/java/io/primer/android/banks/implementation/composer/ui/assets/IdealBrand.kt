package io.primer.android.banks.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.banks.R

internal class IdealBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_ideal
    override val logoResId: Int
        get() = R.drawable.ic_logo_ideal_square
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_ideal_light
    override val iconDarkResId: Int
        get() = R.drawable.ic_logo_ideal_dark
}
