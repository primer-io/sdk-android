package io.primer.android.threeds.domain.models

import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import io.primer.android.threeds.helpers.ProtocolVersion

internal sealed class BaseThreeDsParams(
    open val maxProtocolVersion: ProtocolVersion,
    open val sdkAppId: String,
    open val sdkTransactionId: String,
    open val sdkEncData: String,
    open val sdkEphemPubKey: String,
    open val sdkReferenceNumber: String,
)

internal data class ThreeDsCheckoutParams(
    override val maxProtocolVersion: ProtocolVersion,
    override val sdkAppId: String,
    override val sdkTransactionId: String,
    override val sdkEncData: String,
    override val sdkEphemPubKey: String,
    override val sdkReferenceNumber: String,
) : BaseThreeDsParams(
        maxProtocolVersion = maxProtocolVersion,
        sdkAppId = sdkAppId,
        sdkTransactionId = sdkTransactionId,
        sdkEncData = sdkEncData,
        sdkEphemPubKey = sdkEphemPubKey,
        sdkReferenceNumber = sdkReferenceNumber,
    ) {
    constructor(
        authenticationRequestParameters: AuthenticationRequestParameters,
    ) : this(
        maxProtocolVersion =
            ProtocolVersion.entries
                .first { authenticationRequestParameters.messageVersion == it.versionNumber },
        sdkAppId = authenticationRequestParameters.sdkAppID.orEmpty(),
        sdkTransactionId = authenticationRequestParameters.sdkTransactionID.orEmpty(),
        sdkEncData = authenticationRequestParameters.deviceData.orEmpty(),
        sdkEphemPubKey = authenticationRequestParameters.sdkEphemeralPublicKey.orEmpty(),
        sdkReferenceNumber = authenticationRequestParameters.sdkReferenceNumber.orEmpty(),
    )
}
