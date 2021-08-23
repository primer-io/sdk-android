package io.primer.android.threeds.domain.models

import io.primer.android.UXMode
import io.primer.android.model.dto.CheckoutConfig

internal data class ThreeDsConfigParams(
    val uxMode: UXMode,
    val amount: Int,
    val currency: String,
    val orderId: String,
    val userDetailsAvailable: Boolean,
    val customerFirstName: String,
    val customerLastName: String,
    val customerEmail: String,
    val addressLine1: String,
    val city: String,
    val postalCode: String,
    val countryCode: String,
) {

    constructor(
        checkoutConfig: CheckoutConfig,
    ) : this(
        checkoutConfig.uxMode,
        checkoutConfig.threeDsAmount.amount ?: 0,
        checkoutConfig.threeDsAmount.currency.orEmpty(),
        checkoutConfig.orderId.orEmpty(),
        checkoutConfig.userDetails != null,
        checkoutConfig.userDetails?.firstName.orEmpty(),
        checkoutConfig.userDetails?.lastName.orEmpty(),
        checkoutConfig.userDetails?.email.orEmpty(),
        checkoutConfig.userDetails?.addressLine1.orEmpty(),
        checkoutConfig.userDetails?.city.orEmpty(),
        checkoutConfig.userDetails?.postalCode.orEmpty(),
        checkoutConfig.userDetails?.countryCode?.name.orEmpty(),
    )
}
