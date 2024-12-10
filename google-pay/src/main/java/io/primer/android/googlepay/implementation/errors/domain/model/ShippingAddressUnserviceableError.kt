package io.primer.android.googlepay.implementation.errors.domain.model

import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal class ShippingAddressUnserviceableError(
    shippingMethod: String?
) : PrimerError() {
    override val errorId: String = "shipping-address-unserviceable"
    override val description: String = "The updated shipping address is not serviceable " +
        "with the selected shipping method [$shippingMethod]."
    override val diagnosticsId: String = UUID.randomUUID().toString()
    override val errorCode: String? = null
    override val recoverySuggestion: String = "Please show the GooglePay sheet again with " +
        "valid shipping methods for given address."
    override val exposedError: PrimerError
        get() = this
}
