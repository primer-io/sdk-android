package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class AtomeBrand : Brand {

    override val iconResId: Int
        get() = R.drawable.ic_logo_atome
    override val logoResId: Int
        get() = R.drawable.ic_logo_atome_square
    override val iconDarkResId: Int
        get() = R.drawable.ic_logo_atome_dark
}
