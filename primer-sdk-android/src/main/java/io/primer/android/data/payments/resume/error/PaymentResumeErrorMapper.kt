package io.primer.android.data.payments.resume.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.HttpError
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.ThreeDsError
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.http.exception.HttpException
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsFailedException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.exception.ThreeDsProtocolFailedException

internal class PaymentResumeErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is ThreeDsLibraryNotFoundException -> ThreeDsError.ThreeDsLibraryMissingError
            is ThreeDsLibraryVersionMismatchException -> ThreeDsError.ThreeDsLibraryVersionError(
                throwable.validSdkVersion
            )
            is ThreeDsConfigurationException ->
                ThreeDsError.ThreeDsConfigurationError(throwable.message.orEmpty())
            is ThreeDsInitException -> ThreeDsError.ThreeDsInitError(throwable.message.orEmpty())
            is ThreeDsFailedException -> ThreeDsError.ThreeDsChallengeFailedError(
                throwable.errorCode,
                throwable.message
            )
            is ThreeDsProtocolFailedException -> ThreeDsError.ThreeDsChallengeProtocolFailedError(
                throwable.errorCode,
                throwable.message,
                throwable.context
            )
            is ThreeDsMissingDirectoryServerException ->
                ThreeDsError.ThreeDsMissingDirectoryServerIdError(throwable.cardNetwork)
            is HttpException ->
                when (throwable.isClientError()) {
                    true -> HttpError.HttpClientError(
                        throwable.errorCode,
                        throwable.error.diagnosticsId,
                        throwable.error.description,
                        PaymentError.PaymentResumeFailedError(
                            throwable.error.description,
                            throwable.error.diagnosticsId
                        )
                    )
                    else -> super.getPrimerError(throwable)
                }
            else -> return super.getPrimerError(throwable)
        }
    }
}
