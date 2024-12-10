package io.primer.android.googlepay.implementation.errors.domain.exception

internal class ShippingAddressUnserviceableException(
    val shippingMethod: String?
) : Exception()
