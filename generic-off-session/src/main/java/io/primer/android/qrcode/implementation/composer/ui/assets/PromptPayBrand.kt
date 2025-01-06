package io.primer.android.qrcode.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.offsession.R

internal class PromptPayBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_promptpay_dark

    override val iconLightResId: Int
        get() = R.drawable.ic_logo_promptpay_light
}
