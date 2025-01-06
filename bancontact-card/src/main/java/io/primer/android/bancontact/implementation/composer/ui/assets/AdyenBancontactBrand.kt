package io.primer.android.bancontact.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.bancontact.R

internal class AdyenBancontactBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_bancontact
    override val logoResId: Int
        get() = R.drawable.ic_logo_bancontact_square
    override val iconDarkResId: Int
        get() = R.drawable.ic_logo_bancontact_dark
}
