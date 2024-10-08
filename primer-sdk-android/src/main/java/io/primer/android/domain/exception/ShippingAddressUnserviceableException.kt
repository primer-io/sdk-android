package io.primer.android.domain.exception

internal class ShippingAddressUnserviceableException(
    val shippingMethod: String?
) : Exception()
