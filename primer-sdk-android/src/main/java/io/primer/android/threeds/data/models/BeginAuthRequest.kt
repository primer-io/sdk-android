package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class BeginAuthRequest(
    val maxProtocolVersion: String,
    val challengePreference: ChallengePreference,
    val amount: Int,
    val currencyCode: String,
    val orderId: String,
    val customer: Customer,
    val device: SDKAuthData,
    val billingAddress: Address,
    val shippingAddress: Address? = null,
    val customerAccount: CustomerAccount? = null,
)
