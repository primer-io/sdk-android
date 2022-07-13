package io.primer.android.domain.error.models

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID
import kotlin.reflect.KClass

internal sealed class HUCError : PrimerError() {

    class InitializationError(
        val message: String,
    ) : HUCError()

    object MissingConfigurationError : HUCError()

    class InvalidTokenizationInputDataError(
        val paymentMethodType: PaymentMethodType,
        val inputData: KClass<out PrimerHeadlessUniversalCheckoutInputData>
    ) : HUCError()

    override val errorId: String
        get() = when (this) {
            is InitializationError -> "huc-initialization-failed"
            is MissingConfigurationError -> "huc-missing-configuration"
            is InvalidTokenizationInputDataError -> "huc-invalid-input-data"
        }

    override val description: String
        get() = when (this) {
            is InitializationError ->
                "PrimerHeadlessUniversalCheckout initialization failed" +
                    " | Message: $message"
            is MissingConfigurationError -> "Missing SDK configuration."
            is InvalidTokenizationInputDataError ->
                "PrimerHeadlessUniversalCheckout tokenization error for" +
                    " $paymentMethodType and input data $inputData"
        }

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
                "Make sure you provide data of type $inputData " +
                    "for payment method $paymentMethodType."
        }
}
