package io.primer.android.components.implementation.errors.domain.model

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.paymentmethods.PrimerRawData
import java.util.UUID
import kotlin.reflect.KClass

internal sealed class HeadlessError : PrimerError() {
    class InitializationError(
        val message: String,
    ) : HeadlessError()

    data object InvalidRawDataError : HeadlessError()

    class InvalidTokenizationInputDataError(
        val paymentMethodType: String,
        val inputData: KClass<out PrimerRawData>,
        val requiredInputData: KClass<out PrimerRawData>?,
    ) : HeadlessError() {
        override val context: BaseContextParams
            get() =
                ErrorContextParams(errorId, paymentMethodType)
    }

    override val errorId: String
        get() =
            when (this) {
                is InitializationError -> "huc-initialization-failed"
                is InvalidTokenizationInputDataError -> "invalid-raw-type-data"
                is InvalidRawDataError -> "invalid-raw-data"
            }

    override val description: String
        get() =
            when (this) {
                is InitializationError ->
                    "PrimerHeadlessUniversalCheckout initialization failed" +
                        " | Message: $message"

                is InvalidTokenizationInputDataError ->
                    "PrimerHeadlessUniversalCheckout tokenization error for" +
                        " $paymentMethodType and input data ${inputData.simpleName}"

                is InvalidRawDataError -> "Missing raw data."
            }

    override val errorCode: String? = null

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() =
            when (this) {
                is InitializationError ->
                    "Ensure you are calling 'start' method before calling this method."

                is InvalidTokenizationInputDataError ->
                    "Make sure you provide data of type ${requiredInputData?.simpleName} " +
                        "for payment method $paymentMethodType."

                is InvalidRawDataError -> null
            }
}
