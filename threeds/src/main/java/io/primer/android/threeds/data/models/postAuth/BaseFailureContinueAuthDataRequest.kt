package io.primer.android.threeds.data.models.postAuth

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.threeds.data.models.postAuth.error.BaseContinueAuthErrorDataRequest
import io.primer.android.threeds.data.models.postAuth.error.ChallengeProtocolContinueAuthErrorDataRequest
import io.primer.android.threeds.data.models.postAuth.error.ChallengeRuntimeContinueAuthErrorDataRequest
import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import io.primer.android.threeds.errors.domain.model.ThreeDsError

internal sealed class BaseFailureContinueAuthDataRequest(
    open val error: BaseContinueAuthErrorDataRequest,
) : BaseContinueAuthDataRequest(
    ThreeDsAuthStatus.FAILURE,
) {
    companion object {
        private const val ERROR_FIELD = "error"

        val baseErrorSerializer =
            JSONObjectSerializer<BaseFailureContinueAuthDataRequest> { t ->
                baseSerializer.serialize(t).apply {
                    put(
                        ERROR_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<BaseContinueAuthErrorDataRequest>()
                            .serialize(t.error),
                    )
                }
            }
    }
}

@Suppress("LongMethod")
internal fun FailureThreeDsContinueAuthParams.toContinueAuthDataRequest() =
    when (error) {
        is ThreeDsError.ThreeDsLibraryMissingError ->
            MissingDependencyFailureContinueAuthDataRequest(
                PreChallengeContinueAuthErrorDataRequest(
                    ThreeDsSdkErrorReasonCode.MISSING_SDK_DEPENDENCY,
                    error.description,
                    error.recoverySuggestion,
                ),
            )

        else ->
            DefaultFailureContinueAuthDataRequest(
                threeDsSdkVersion,
                initProtocolVersion,
                when (error) {
                    is ThreeDsError.ThreeDsChallengeFailedError ->
                        ChallengeRuntimeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.`3DS_SDK_RUNTIME_ERROR`,
                            error.description,
                            error.context.errorCode,
                            error.description,
                        )

                    is ThreeDsError.ThreeDsChallengeInvalidStatusError ->
                        ChallengeRuntimeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.INVALID_CHALLENGE_STATUS,
                            error.description,
                            error.errorCode,
                            error.description,
                        )

                    is ThreeDsError.ThreeDsChallengeCancelledError ->
                        ChallengeRuntimeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.CHALLENGE_CANCELLED_BY_USER,
                            error.description,
                            error.errorCode,
                            error.description,
                        )

                    is ThreeDsError.ThreeDsChallengeTimedOutError ->
                        ChallengeRuntimeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.CHALLENGE_TIMED_OUT,
                            error.description,
                            error.errorCode,
                            error.description,
                        )

                    is ThreeDsError.ThreeDsChallengeProtocolFailedError ->
                        ChallengeProtocolContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.`3DS_SDK_PROTOCOL_ERROR`,
                            error.description,
                            error.context.errorCode,
                            error.context.description,
                            error.context.component,
                            error.context.errorDetails,
                            error.context.transactionId,
                            error.context.version,
                        )

                    is ThreeDsError.ThreeDsConfigurationError ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.MISSING_3DS_CONFIGURATION,
                            error.description,
                            error.recoverySuggestion,
                        )

                    is ThreeDsError.ThreeDsInitError ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.`3DS_SDK_INIT_FAILED`,
                            error.description,
                            error.recoverySuggestion,
                        )

                    is ThreeDsError.ThreeDsLibraryVersionError ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.INVALID_3DS_SDK_VERSION,
                            error.description,
                            error.recoverySuggestion,
                        )

                    is ThreeDsError.ThreeDsMissingDirectoryServerIdError ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.MISSING_DS_RID,
                            error.description,
                            error.recoverySuggestion,
                        )

                    is ThreeDsError.ThreeDsUnknownProtocolError ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.UNKNOWN_PROTOCOL_VERSION,
                            error.description,
                            error.recoverySuggestion,
                        )

                    else ->
                        PreChallengeContinueAuthErrorDataRequest(
                            ThreeDsSdkErrorReasonCode.`3DS_UNKNOWN_ERROR`,
                            error.description,
                            error.recoverySuggestion,
                        )
                },
            )
    }
