package io.primer.android.threeds.domain.models

import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.threeds.data.models.Address
import io.primer.android.threeds.data.models.BeginAuthRequest
import io.primer.android.threeds.data.models.ChallengePreference
import io.primer.android.threeds.data.models.Customer
import io.primer.android.threeds.data.models.SDKAuthData
import io.primer.android.threeds.helpers.ProtocolVersion

private const val SDK_TIMEOUT_IN_SECONDS = 60

internal data class ThreeDsParams(
    val paymentMethodIntent: PrimerPaymentMethodIntent,
    val maxProtocolVersion: ProtocolVersion,
    val challengePreference: ChallengePreference,
    val amount: Int?,
    val currency: String?,
    val orderId: String?,
    val customerName: String?,
    val customerEmail: String?,
    val sdkAppId: String,
    val sdkTransactionId: String,
    val sdkEncData: String,
    val sdkEphemPubKey: String,
    val sdkReferenceNumber: String,
    val addressLine1: String,
    val city: String,
    val postalCode: String,
    val countryCode: String,
) {

    constructor(
        authenticationRequestParameters: AuthenticationRequestParameters,
        config: PrimerConfig,
        challengePreference: ChallengePreference,
    ) : this(
        config.paymentMethodIntent,
        ProtocolVersion.values()
            .first { authenticationRequestParameters.messageVersion == it.versionNumber },
        challengePreference,
        config.settings.currentAmount,
        config.settings.currency,
        config.settings.order.id,
        config.settings.customer.firstName,
        config.settings.customer.emailAddress,
        authenticationRequestParameters.sdkAppID.orEmpty(),
        authenticationRequestParameters.sdkTransactionID.orEmpty(),
        authenticationRequestParameters.deviceData.orEmpty(),
        authenticationRequestParameters.sdkEphemeralPublicKey.orEmpty(),
        authenticationRequestParameters.sdkReferenceNumber.orEmpty(),
        config.settings.customer.billingAddress?.addressLine1.orEmpty(),
        config.settings.customer.billingAddress?.city.orEmpty(),
        config.settings.customer.billingAddress?.postalCode.orEmpty(),
        config.settings.customer.billingAddress?.countryCode?.name.orEmpty(),
    )
}

internal fun ThreeDsParams.toBeginAuthRequest() = when (paymentMethodIntent) {
    PrimerPaymentMethodIntent.CHECKOUT -> BeginAuthRequest(
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
    PrimerPaymentMethodIntent.VAULT -> BeginAuthRequest(
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
