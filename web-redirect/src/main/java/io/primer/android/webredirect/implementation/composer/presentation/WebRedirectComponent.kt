package io.primer.android.webredirect.implementation.composer.presentation

import androidx.annotation.VisibleForTesting
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.webRedirectShared.implementation.composer.presentation.BaseWebRedirectComposer
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.android.webredirect.implementation.payment.presentation.delegate.presentation.WebRedirectPaymentDelegate
import io.primer.android.webredirect.implementation.tokenization.presentation.WebRedirectTokenizationDelegate
import io.primer.android.webredirect.implementation.tokenization.presentation.model.WebRedirectTokenizationInputable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

internal class WebRedirectComponent(
    private val tokenizationDelegate: WebRedirectTokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: WebRedirectPaymentDelegate,
) : InternalNativeUiPaymentMethodComponent(),
    BaseWebRedirectComposer {
    override val scope: CoroutineScope
        get() = composerScope

    override val _uiEvent: MutableSharedFlow<ComposerUiEvent> = MutableSharedFlow()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent
        composerScope.launch {
            paymentDelegate.uiEvent.collectLatest { uiEvent ->
                _uiEvent.emit(uiEvent)
            }
        }
        tokenize(paymentMethodType)
    }

    override fun onResultCancelled(params: WebRedirectLauncherParams) {
        composerScope.launch {
            paymentDelegate.handleError(
                PaymentMethodCancelledException(
                    params.paymentMethodType,
                ),
            )
        }
    }

    override fun onResultOk(params: WebRedirectLauncherParams) {
        startPolling(params.statusUrl, params.paymentMethodType)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(
        statusUrl: String,
        paymentMethodType: String,
    ) = composerScope.launch {
        pollingInteractor.execute(
            AsyncStatusParams(statusUrl, paymentMethodType),
        ).mapLatest { status ->
            paymentDelegate.resumePayment(status.resumeToken)
        }.catch {
            paymentDelegate.handleError(it)
        }.collect()
    }

    private fun tokenize(paymentMethodType: String) =
        composerScope.launch {
            tokenizationDelegate.tokenize(
                WebRedirectTokenizationInputable(
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
