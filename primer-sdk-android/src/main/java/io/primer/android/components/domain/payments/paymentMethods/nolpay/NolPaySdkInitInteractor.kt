package io.primer.android.components.domain.payments.paymentMethods.nolpay

import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.core.logging.PrimerLogLevel
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.Environment
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.base.None
import io.primer.android.extensions.onError
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class NolPaySdkInitInteractor(
    private val secretRepository: NolPayAppSecretRepository,
    private val nolPayConfigurationRepository: NolPayConfigurationRepository,
    private val nolPay: PrimerNolPay,
    private val logReporter: LogReporter,
    private val nolSdkInitDispatcher: CoroutineDispatcher = Dispatchers.Main,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, None>() {

    private var nolSdkStatus: NolSdkStatus = NolSdkStatus.NOT_INITIALIZED

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                secretRepository.getAppSecret(
                    sdkId,
                    nolPayConfigurationRepository.getConfiguration().getOrThrow().merchantAppId
                ).getOrThrow()
            }
        }
    }

    override suspend fun performAction(params: None) = runSuspendCatching {
        nolSdkStatus.takeIf { status -> status == NolSdkStatus.NOT_INITIALIZED }?.let {
            nolSdkStatus = NolSdkStatus.INITIALIZING
            val configuration = nolPayConfigurationRepository.getConfiguration().getOrThrow()
            withContext(nolSdkInitDispatcher) {
                nolPay.initSDK(
                    configuration.environment != Environment.PRODUCTION,
                    logReporter.logLevel == PrimerLogLevel.DEBUG,
                    configuration.merchantAppId,
                    handler
                ).let { }
            }
        } ?: Unit
    }.onSuccess {
        nolSdkStatus = NolSdkStatus.INITIALIZED
    }.onError { throwable ->
        nolSdkStatus = NolSdkStatus.NOT_INITIALIZED
        throwable
    }
}

private enum class NolSdkStatus {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED
}
