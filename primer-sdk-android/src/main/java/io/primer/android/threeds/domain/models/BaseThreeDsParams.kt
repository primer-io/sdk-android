package io.primer.android.threeds.domain.models

import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.threeds.data.models.auth.ChallengePreference
import io.primer.android.threeds.helpers.ProtocolVersion

internal sealed class BaseThreeDsParams(
    open val maxProtocolVersion: ProtocolVersion,
    open val challengePreference: ChallengePreference,
    open val sdkAppId: String,
    open val sdkTransactionId: String,
    open val sdkEncData: String,
    open val sdkEphemPubKey: String,
    open val sdkReferenceNumber: String
)

internal data class ThreeDsCheckoutParams(
    override val maxProtocolVersion: ProtocolVersion,
    override val challengePreference: ChallengePreference,
    override val sdkAppId: String,
    override val sdkTransactionId: String,
    override val sdkEncData: String,
    override val sdkEphemPubKey: String,
    override val sdkReferenceNumber: String
) : BaseThreeDsParams(
    maxProtocolVersion,
    challengePreference,
    sdkAppId,
    sdkTransactionId,
    sdkEncData,
    sdkEphemPubKey,
    sdkReferenceNumber
) {

    constructor(
        authenticationRequestParameters: AuthenticationRequestParameters,
        challengePreference: ChallengePreference
    ) : this(
        ProtocolVersion.values()
            .first { authenticationRequestParameters.messageVersion == it.versionNumber },
        challengePreference,
        authenticationRequestParameters.sdkAppID.orEmpty(),
        authenticationRequestParameters.sdkTransactionID.orEmpty(),
        authenticationRequestParameters.deviceData.orEmpty(),
        authenticationRequestParameters.sdkEphemeralPublicKey.orEmpty(),
        authenticationRequestParameters.sdkReferenceNumber.orEmpty()
    )
}

internal data class ThreeDsVaultParams(
    override val maxProtocolVersion: ProtocolVersion,
    override val challengePreference: ChallengePreference,
    val amount: Int?,
    val currency: String?,
    val orderId: String?,
    val customerName: String?,
    val customerEmail: String?,
    override val sdkAppId: String,
    override val sdkTransactionId: String,
    override val sdkEncData: String,
    override val sdkEphemPubKey: String,
    override val sdkReferenceNumber: String,
    val addressLine1: String,
    val city: String,
    val postalCode: String,
    val countryCode: String
) : BaseThreeDsParams(
    maxProtocolVersion,
    challengePreference,
    sdkAppId,
    sdkTransactionId,
    sdkEncData,
    sdkEphemPubKey,
    sdkReferenceNumber
) {

    constructor(
        authenticationRequestParameters: AuthenticationRequestParameters,
        config: PrimerConfig,
        challengePreference: ChallengePreference
    ) : this(
        ProtocolVersion.values()
            .first { authenticationRequestParameters.messageVersion == it.versionNumber },
        challengePreference,
        config.settings.currentAmount,
        config.settings.currency,
        config.settings.order.orderId,
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
        config.settings.customer.billingAddress?.countryCode?.name.orEmpty()
    )
}
