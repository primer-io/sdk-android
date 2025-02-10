package io.primer.android.errors.data.mapper

import io.primer.android.configuration.data.exception.MissingConfigurationException
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.data.network.exception.InvalidUrlException
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.exception.JsonEncodingException
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.errors.data.exception.SessionUpdateException
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.errors.domain.models.ClientError
import io.primer.android.errors.domain.models.ConnectivityError
import io.primer.android.errors.domain.models.GeneralError
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.errors.domain.models.ParserError
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.errors.domain.models.SessionCreateError
import io.primer.android.errors.domain.models.SessionUpdateError
import java.io.IOException

internal class DefaultErrorMapper : ErrorMapper {
    @Suppress("ComplexMethod", "LongMethod")
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is IOException -> ConnectivityError(throwable.message.orEmpty())
            is JsonEncodingException -> ParserError.EncodeError(throwable.message.orEmpty())
            is JsonDecodingException -> ParserError.DecodeError(throwable.message.orEmpty())
            is SessionCreateException ->
                SessionCreateError(
                    throwable.paymentMethodType,
                    throwable.diagnosticsId,
                    throwable.description,
                )

            is SessionUpdateException ->
                SessionUpdateError(
                    throwable.diagnosticsId,
                    throwable.description,
                )

            is HttpException -> {
                return when {
                    throwable.isUnAuthorizedError() ->
                        HttpError.HttpUnauthorizedError(
                            throwable.errorCode,
                            throwable.error.diagnosticsId,
                        )

                    throwable.isServerError() ->
                        HttpError.HttpServerError(
                            throwable.errorCode,
                            throwable.error.diagnosticsId,
                            throwable.error.description,
                        )

                    throwable.isClientError() ->
                        HttpError.HttpClientError(
                            throwable.errorCode,
                            throwable.error.diagnosticsId,
                            throwable.error.description,
                            ClientError(
                                throwable.error.description,
                                throwable.error.diagnosticsId.orEmpty(),
                            ),
                        )

                    else -> PrimerUnknownError(throwable.message.orEmpty())
                }
            }

            is PaymentMethodCancelledException ->
                PaymentMethodCancelledError(
                    throwable.paymentMethodType,
                )

            is MissingConfigurationException -> GeneralError.MissingConfigurationError
            is IllegalValueException ->
                GeneralError.InvalidValueError(
                    throwable.key,
                    throwable.message,
                )

            is InvalidUrlException -> GeneralError.InvalidUrlError(throwable.message.orEmpty())

            is IllegalClientSessionValueException ->
                GeneralError.InvalidClientSessionValueError(
                    throwable.key,
                    throwable.value,
                    throwable.allowedValue,
                    throwable.message,
                )

            else -> throw IllegalArgumentException("Unsupported mapping for $throwable")
        }
    }
}
