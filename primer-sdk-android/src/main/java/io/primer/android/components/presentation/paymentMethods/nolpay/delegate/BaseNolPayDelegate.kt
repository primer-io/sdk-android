package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.collect

internal open class BaseNolPayDelegate(
    private val sdkInitInteractor: NolPaySdkInitInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
) {
    suspend fun start() = sdkInitInteractor(None())

    suspend fun logSdkAnalyticsEvent(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) {
        analyticsInteractor(
            SdkFunctionParams(
                methodName,
                mapOf(
                    "category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name
                ).plus(context)
            )
        ).collect()
    }

    suspend fun logSdkAnalyticsErrors(
        error: PrimerError,
    ) = analyticsInteractor(
        MessageAnalyticsParams(
            MessageType.ERROR,
            error.description,
            Severity.ERROR,
            error.diagnosticsId
        )
    ).collect()
}
