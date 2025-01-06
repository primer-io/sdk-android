package io.primer.android.vouchers.multibanco.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.offsession.R

internal class MultibancoBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_multibanco_dark

    override val iconLightResId: Int
        get() = R.drawable.ic_logo_multibanco_light
}
