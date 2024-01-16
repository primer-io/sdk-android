package io.primer.android.components.presentation.paymentMethods.analytics.delegate

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import kotlinx.coroutines.flow.collect

internal class PaymentMethodSdkAnalyticsEventLoggingDelegate(
    private val primerPaymentMethodManagerCategory: PrimerPaymentMethodManagerCategory,
    private val analyticsInteractor: AnalyticsInteractor
) {
    suspend fun logSdkAnalyticsEvent(
        methodName: String,
        paymentMethodType: String,
        context: Map<String, String> = emptyMap()
    ) {
        analyticsInteractor(
            SdkFunctionParams(
                name = methodName,
                params = mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to primerPaymentMethodManagerCategory.name
                ) + context
            )
        ).collect()
    }
}
