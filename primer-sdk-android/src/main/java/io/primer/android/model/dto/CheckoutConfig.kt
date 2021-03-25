package io.primer.android.model.dto

import io.primer.android.UXMode
import io.primer.android.UniversalCheckoutTheme
import kotlinx.serialization.Serializable

@Serializable
internal data class CheckoutConfig(
    val clientToken: String,
    val uxMode: UXMode,
    val theme: UniversalCheckoutTheme,
    val amount: MonetaryAmount?,
) {

    constructor(
        clientToken: String,
        uxMode: UXMode = UXMode.CHECKOUT,
        currency: String? = null,
        amount: Int? = null,
        theme: UniversalCheckoutTheme? = null,
    ) : this(
        clientToken,
        uxMode = uxMode,
        theme = theme ?: UniversalCheckoutTheme.getDefault(),
        amount = MonetaryAmount.create(currency = currency, value = amount)
    )
}
