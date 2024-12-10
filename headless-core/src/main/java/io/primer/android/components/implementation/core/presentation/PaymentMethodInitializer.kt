package io.primer.android.components.implementation.core.presentation

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException
import io.primer.android.components.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory

internal interface PaymentMethodInitializer {
    @Throws(
        SdkUninitializedException::class,
        UnsupportedPaymentMethodManagerException::class,
        UnsupportedPaymentMethodException::class
    )
    suspend fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory)
}

internal class DefaultPaymentMethodInitializer(
    private val initValidationRulesResolver: PaymentMethodManagerInitValidationRulesResolver,
    private val analyticsInteractor: AnalyticsInteractor
) : PaymentMethodInitializer {

    override suspend fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory) {
        addAnalyticsEvent(
            SdkFunctionParams(
                "newInstance",
                mapOf("paymentMethodType" to paymentMethodType, "category" to category.name)
            )
        )

        val validationResults = initValidationRulesResolver.resolve().rules.map {
            it.validate(
                PaymentMethodManagerInitValidationData(
                    paymentMethodType,
                    category
                )
            )
        }

        validationResults.filterIsInstance<ValidationResult.Failure>()
            .forEach { validationResult ->
                throw validationResult.exception
            }
    }

    private suspend fun addAnalyticsEvent(params: BaseAnalyticsParams) = analyticsInteractor(params)
}
