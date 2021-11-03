package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class BeginAuthRequest(
    val maxProtocolVersion: String,
    val challengePreference: ChallengePreference,
    val amount: Int? = null,
    val currencyCode: String ? = null,
    val orderId: String? = null,
    val customer: Customer? = null,
    val device: SDKAuthData,
    val billingAddress: Address? = null,
    val shippingAddress: Address? = null,
    val customerAccount: CustomerAccount? = null,
)
