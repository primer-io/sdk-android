package io.primer.android.threeds.data.models

import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.domain.models.ThreeDsVaultParams
import kotlinx.serialization.Serializable

private const val SDK_TIMEOUT_IN_SECONDS = 60

@Serializable
internal data class BeginAuthRequest(
    val maxProtocolVersion: String,
    val challengePreference: ChallengePreference,
    val amount: Int? = null,
    val currencyCode: String? = null,
    val orderId: String? = null,
    val customer: Customer? = null,
    val device: SDKAuthData,
    val billingAddress: Address? = null,
    val shippingAddress: Address? = null,
    val customerAccount: CustomerAccount? = null,
)

internal fun BaseThreeDsParams.toBeginAuthRequest(): BeginAuthRequest {
    return when (this) {
        is ThreeDsCheckoutParams -> BeginAuthRequest(
            maxProtocolVersion.versionNumber,
            challengePreference,
            device = SDKAuthData(
                sdkAppId,
                sdkTransactionId,
                SDK_TIMEOUT_IN_SECONDS,
                sdkEncData,
                sdkEphemPubKey,
                sdkReferenceNumber
            ),
        )
        is ThreeDsVaultParams -> BeginAuthRequest(
            maxProtocolVersion.versionNumber,
            challengePreference,
            amount,
            currency,
            orderId,
            Customer(customerName, customerEmail),
            SDKAuthData(
                sdkAppId,
                sdkTransactionId,
                SDK_TIMEOUT_IN_SECONDS,
                sdkEncData,
                sdkEphemPubKey,
                sdkReferenceNumber
            ),
            Address(
                addressLine1 = addressLine1,
                city = city,
                postalCode = postalCode,
                countryCode = countryCode
            ),
        )
    }
}
