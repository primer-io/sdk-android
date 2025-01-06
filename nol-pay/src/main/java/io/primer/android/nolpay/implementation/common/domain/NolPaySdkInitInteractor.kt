package io.primer.android.nolpay.implementation.common.domain

import androidx.annotation.VisibleForTesting
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.PrimerLogLevel
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayAppSecretRepository
import io.primer.android.nolpay.implementation.common.domain.repository.NolPaySdkInitConfigurationRepository
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class NolPaySdkInitInteractor(
    private val secretRepository: NolPayAppSecretRepository,
    private val nolPaySdkInitConfigurationRepository: NolPaySdkInitConfigurationRepository,
    private val nolPay: PrimerNolPay,
    private val logReporter: LogReporter,
    private val nolSdkInitDispatcher: CoroutineDispatcher = Dispatchers.Main,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Unit, None>() {
    private var nolSdkStatus: NolSdkStatus = NolSdkStatus.NOT_INITIALIZED

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val handler =
        object : TransitAppSecretKeyHandler {
            override fun getAppSecretKeyFromServer(sdkId: String): String {
                return runBlocking {
                    secretRepository.getAppSecret(
                        sdkId,
                        nolPaySdkInitConfigurationRepository.getConfiguration().getOrThrow().merchantAppId,
                    ).getOrThrow()
                }
            }
        }

    override suspend fun performAction(params: None) =
        runSuspendCatching {
            nolSdkStatus.takeIf { status -> status == NolSdkStatus.NOT_INITIALIZED }?.let {
                nolSdkStatus = NolSdkStatus.INITIALIZING
                val configuration = nolPaySdkInitConfigurationRepository.getConfiguration().getOrThrow()
                withContext(nolSdkInitDispatcher) {
                    nolPay.initSDK(
                        configuration.environment != Environment.PRODUCTION,
                        logReporter.logLevel == PrimerLogLevel.DEBUG,
                        configuration.merchantAppId,
                        handler,
                    ).let { }
                }
            } ?: Unit
        }.onSuccess {
            nolSdkStatus = NolSdkStatus.INITIALIZED
        }.onError { throwable ->
            nolSdkStatus = NolSdkStatus.NOT_INITIALIZED
            throw throwable
        }
}

private enum class NolSdkStatus {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
}
