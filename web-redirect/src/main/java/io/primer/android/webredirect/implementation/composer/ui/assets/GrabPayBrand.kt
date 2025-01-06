package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.assets.ui.model.Brand
import io.primer.android.webredirect.R

internal class GrabPayBrand : Brand {
    override val iconResId: Int
        get() = R.drawable.ic_logo_grab_pay
    override val iconLightResId: Int
        get() = R.drawable.ic_logo_grab_pay_light
    override val iconDarkResId: Int
        get() = R.drawable.ic_logo_grab_pay_dark
}
