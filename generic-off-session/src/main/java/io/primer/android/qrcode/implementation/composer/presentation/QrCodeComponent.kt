package io.primer.android.qrcode.implementation.composer.presentation

import androidx.annotation.VisibleForTesting
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.errors.extensions.onFailureWithCancellation
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.qrcode.implementation.payment.delegate.QrCodePaymentDelegate
import io.primer.android.qrcode.implementation.tokenization.presentation.QrCodeTokenizationDelegate
import io.primer.android.qrcode.implementation.tokenization.presentation.composable.QrCodeTokenizationInputable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

internal class QrCodeComponent(
    private val tokenizationDelegate: QrCodeTokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: QrCodePaymentDelegate,
    private val pollingStartHandler: PollingStartHandler,
) : InternalNativeUiPaymentMethodComponent() {
    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent
        composerScope.launch {
            pollingStartHandler.startPolling.collectLatest { pollingStartData ->
                startPolling(
                    url = pollingStartData.statusUrl,
                    paymentMethodType = pollingStartData.paymentMethodType,
                )
            }
        }
        tokenize()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(
        url: String,
        paymentMethodType: String,
    ) = composerScope.launch {
        runCatching {
            pollingInteractor.execute(
                AsyncStatusParams(url, paymentMethodType),
            ).mapLatest { status ->
                paymentDelegate.resumePayment(status.resumeToken)
            }.catch {
                paymentDelegate.handleError(it)
            }.collect()
        }.onFailureWithCancellation(paymentMethodType, paymentDelegate::handleError)
    }

    private fun tokenize() =
        composerScope.launch {
            tokenizationDelegate.tokenize(
                QrCodeTokenizationInputable(
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent,
                ),
            )
                .flatMap { paymentMethodTokenData ->
                    paymentDelegate.handlePaymentMethodToken(
                        paymentMethodTokenData = paymentMethodTokenData,
                        primerSessionIntent = primerSessionIntent,
                    )
                }.onFailure { throwable ->
                    paymentDelegate.handleError(throwable)
                }
        }
}
