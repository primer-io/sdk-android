package io.primer.android.domain.error.models

import io.primer.android.components.domain.core.models.PrimerRawData
import java.util.UUID
import kotlin.reflect.KClass

internal sealed class HUCError : PrimerError() {

    class InitializationError(
        val message: String
    ) : HUCError()

    object MissingConfigurationError : HUCError()

    object InvalidRawDataError : HUCError()

    class InvalidTokenizationInputDataError(
        val paymentMethodType: String,
        val inputData: KClass<out PrimerRawData>,
        val requiredInputData: KClass<out PrimerRawData>?
    ) : HUCError()

    override val errorId: String
        get() = when (this) {
            is InitializationError -> "huc-initialization-failed"
            is MissingConfigurationError -> "huc-missing-configuration"
            is InvalidTokenizationInputDataError -> "huc-invalid-raw-type-data"
            is InvalidRawDataError -> "invalid-raw-data"
        }

    override val description: String
        get() = when (this) {
            is InitializationError ->
                "PrimerHeadlessUniversalCheckout initialization failed" +
                    " | Message: $message"
            is MissingConfigurationError -> "Missing SDK configuration."
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
        get() = when (this) {
            is InitializationError ->
                "Please ensure you are calling 'start' method before calling this method."
            is MissingConfigurationError ->
                "Please ensure that you have an active internet connection." +
                    " Contact Primer and provide us with diagnostics id $diagnosticsId"
            is InvalidTokenizationInputDataError ->
                "Make sure you provide data of type ${requiredInputData?.simpleName} " +
                    "for payment method $paymentMethodType."
            is InvalidRawDataError -> null
        }
}
