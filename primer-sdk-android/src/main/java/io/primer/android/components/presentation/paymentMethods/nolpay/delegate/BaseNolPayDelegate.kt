package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.BuildConfig
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.data.configuration.models.Environment
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking

internal open class BaseNolPayDelegate(
    private val appSecretInteractor: NolPayAppSecretInteractor,
    private val configurationInteractor: NolPayConfigurationInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
) {

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                appSecretInteractor(NolPaySecretParams(sdkId)).getOrThrow()
                // .getOrElse { "dc0c17c205d740469d5a4e1e38233ba2" }
            }
        }
    }

    suspend fun start() = runSuspendCatching {
        configurationInteractor(None()).collectLatest { configuration ->
            PrimerNolPay.initSDK(
                configuration.environment != Environment.PRODUCTION,
                BuildConfig.DEBUG,
                configuration.merchantAppId,
                handler
            )
        }
    }

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
