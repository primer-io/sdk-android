package io.primer.android.klarna.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.klarna.main.R

internal class KlarnaBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_klarna

    override val logoResId: Int
        get() = R.drawable.ic_logo_klarna_square

    override val iconDarkResId: Int
        get() = R.drawable.ic_logo_klarna_dark
}
