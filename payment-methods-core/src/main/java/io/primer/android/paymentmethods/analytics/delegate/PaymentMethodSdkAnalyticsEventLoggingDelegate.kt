package io.primer.android.paymentmethods.analytics.delegate

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams

class PaymentMethodSdkAnalyticsEventLoggingDelegate(
    private val primerPaymentMethodManagerCategory: String,
    private val analyticsInteractor: AnalyticsInteractor,
) {
    suspend fun logSdkAnalyticsEvent(
        methodName: String,
        paymentMethodType: String,
        context: Map<String, String> = emptyMap(),
    ) {
        analyticsInteractor(
            params =
                SdkFunctionParams(
                    name = methodName,
                    params =
                        mapOf(
                            "paymentMethodType" to paymentMethodType,
                            "category" to primerPaymentMethodManagerCategory,
                        ) + context,
                ),
        )
    }
}
