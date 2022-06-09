package io.primer.android.data.error

import io.primer.android.http.exception.HttpException
import io.primer.android.http.exception.JsonDecodingException
import io.primer.android.http.exception.JsonEncodingException
import io.primer.android.data.configuration.exception.MissingConfigurationException
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.token.exception.ExpiredClientTokenException
import io.primer.android.data.token.exception.InvalidClientTokenException
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.ClientError
import io.primer.android.domain.error.models.ClientTokenError
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.ConnectivityError
import io.primer.android.domain.error.models.HttpError
import io.primer.android.domain.error.models.ParserError
import io.primer.android.domain.error.models.PaymentMethodError
import java.io.IOException

internal open class DefaultErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is IOException -> ConnectivityError(throwable.message.orEmpty())
            is JsonEncodingException -> ParserError.EncodeError(throwable.message.orEmpty())
            is JsonDecodingException -> ParserError.DecodeError(throwable.message.orEmpty())
            is HttpException -> {
                return when {
                    throwable.isUnAuthorizedError() -> HttpError.HttpUnauthorizedError(
                        throwable.errorCode,
                        throwable.error.diagnosticsId
                    )
                    throwable.isServerError() -> HttpError.HttpServerError(
                        throwable.errorCode,
                        throwable.error.diagnosticsId,
                        throwable.error.description
                    )
                    throwable.isClientError() -> HttpError.HttpClientError(
                        throwable.errorCode,
                        throwable.error.diagnosticsId,
                        throwable.error.description,
                        ClientError(
                            throwable.error.description,
                            throwable.error.diagnosticsId.orEmpty()
                        )
                    )
                    else -> GeneralError.UnknownError(throwable.message.orEmpty())
                }
            }
            is PaymentMethodCancelledException ->
                PaymentMethodError.PaymentMethodCancelledError(throwable.paymentMethodType)
            is InvalidClientTokenException -> ClientTokenError.InvalidClientTokenError
            is ExpiredClientTokenException -> ClientTokenError.ExpiredClientTokenError
            is MissingConfigurationException -> GeneralError.MissingConfigurationError
            else -> GeneralError.UnknownError(throwable.message.orEmpty())
        }
    }
}
