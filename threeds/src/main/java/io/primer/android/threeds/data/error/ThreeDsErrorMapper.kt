package io.primer.android.threeds.data.error

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.threeds.data.exception.ThreeDsChallengeCancelledException
import io.primer.android.threeds.data.exception.ThreeDsChallengeTimedOutException
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsInvalidStatusException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.exception.ThreeDsProtocolFailedException
import io.primer.android.threeds.data.exception.ThreeDsRuntimeFailedException
import io.primer.android.threeds.data.exception.ThreeDsUnknownProtocolException
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.threeds.errors.domain.model.ThreeDsError

internal class ThreeDsErrorMapper : ErrorMapper {
    @Suppress("LongMethod")
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is ThreeDsLibraryNotFoundException -> ThreeDsError.ThreeDsLibraryMissingError
            is ThreeDsLibraryVersionMismatchException ->
                ThreeDsError.ThreeDsLibraryVersionError(
                    validSdkVersion = throwable.validSdkVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                )

            is ThreeDsConfigurationException ->
                ThreeDsError.ThreeDsConfigurationError(
                    message = throwable.message.orEmpty(),
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                )

            is ThreeDsInitException ->
                ThreeDsError.ThreeDsInitError(
                    message = throwable.message.orEmpty(),
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                )

            is ThreeDsChallengeTimedOutException ->
                ThreeDsError.ThreeDsChallengeTimedOutError(
                    message = throwable.message,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                    threeDsErrorCode = throwable.errorCode,
                )

            is ThreeDsChallengeCancelledException ->
                ThreeDsError.ThreeDsChallengeCancelledError(
                    message = throwable.message,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                    threeDsErrorCode = throwable.errorCode,
                )

            is ThreeDsInvalidStatusException ->
                ThreeDsError.ThreeDsChallengeInvalidStatusError(
                    message = throwable.message,
                    transactionStatus = throwable.transactionStatus,
                    transactionId = throwable.transactionId,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                    threeDsErrorCode = throwable.errorCode,
                )

            is ThreeDsRuntimeFailedException ->
                ThreeDsError.ThreeDsChallengeFailedError(
                    message = throwable.message,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                    threeDsErrorCode = throwable.errorCode,
                )

            is ThreeDsProtocolFailedException ->
                ThreeDsError.ThreeDsChallengeProtocolFailedError(
                    message = throwable.message,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                    threeDsErrorCode = throwable.errorCode,
                    threeDsErrorDetails = throwable.errorDetails,
                    threeDsErrorMessageType = throwable.messageType,
                    threeDsComponent = throwable.component,
                    threeDsDescription = throwable.description,
                    threeDsProtocolVersion = throwable.version,
                    threeDsTransactionId = throwable.transactionId,
                )

            is ThreeDsMissingDirectoryServerException ->
                ThreeDsError.ThreeDsMissingDirectoryServerIdError(
                    throwable.cardNetwork,
                    threeDsSdkVersion = throwable.threeDsSdkVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                )

            is ThreeDsUnknownProtocolException ->
                ThreeDsError.ThreeDsUnknownProtocolError(
                    initProtocolVersion = throwable.initProtocolVersion,
                    threeDsWrapperSdkVersion = throwable.threeDsWrapperSdkVersion,
                    threeDsSdkProvider = throwable.threeDsSdkProvider,
                )

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
