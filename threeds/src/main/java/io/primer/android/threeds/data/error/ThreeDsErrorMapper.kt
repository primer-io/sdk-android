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
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is ThreeDsLibraryNotFoundException -> ThreeDsError.ThreeDsLibraryMissingError
            is ThreeDsLibraryVersionMismatchException ->
                ThreeDsError.ThreeDsLibraryVersionError(
                    throwable.validSdkVersion,
                    throwable.context,
                )

            is ThreeDsConfigurationException ->
                ThreeDsError.ThreeDsConfigurationError(
                    throwable.message.orEmpty(),
                    throwable.context,
                )

            is ThreeDsInitException ->
                ThreeDsError.ThreeDsInitError(
                    throwable.message.orEmpty(),
                    throwable.context,
                )

            is ThreeDsChallengeTimedOutException ->
                ThreeDsError.ThreeDsChallengeTimedOutError(
                    throwable.errorCode,
                    throwable.message,
                    throwable.context,
                )

            is ThreeDsChallengeCancelledException ->
                ThreeDsError.ThreeDsChallengeCancelledError(
                    throwable.errorCode,
                    throwable.message,
                    throwable.context,
                )

            is ThreeDsInvalidStatusException ->
                ThreeDsError.ThreeDsChallengeInvalidStatusError(
                    throwable.transactionStatus,
                    throwable.transactionId,
                    throwable.errorCode,
                    throwable.message,
                    throwable.context,
                )

            is ThreeDsRuntimeFailedException ->
                ThreeDsError.ThreeDsChallengeFailedError(
                    throwable.message,
                    throwable.context,
                )

            is ThreeDsProtocolFailedException ->
                ThreeDsError.ThreeDsChallengeProtocolFailedError(
                    throwable.errorCode,
                    throwable.message,
                    throwable.context,
                )

            is ThreeDsMissingDirectoryServerException ->
                ThreeDsError.ThreeDsMissingDirectoryServerIdError(
                    throwable.cardNetwork,
                    throwable.context,
                )

            is ThreeDsUnknownProtocolException ->
                ThreeDsError.ThreeDsUnknownProtocolError(
                    throwable.initProtocolVersion,
                    throwable.context,
                )

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
