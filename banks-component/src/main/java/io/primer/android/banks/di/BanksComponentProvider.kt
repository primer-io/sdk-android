package io.primer.android.banks.di

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.banks.implementation.payment.presentation.delegate.presentation.BankIssuerPaymentDelegate
import io.primer.android.banks.implementation.tokenization.presentation.BankIssuerTokenizationDelegate
import io.primer.android.banks.implementation.tokenization.presentation.model.BankIssuerTokenizationInputable
import io.primer.android.components.manager.banks.component.DefaultBanksComponent
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.extensions.flatMap
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.webRedirectShared.di.WebRedirectContainer
import io.primer.android.webRedirectShared.implementation.composer.presentation.BaseWebRedirectComposer
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class BankWebRedirectComposer(
    private val tokenizationDelegate: BankIssuerTokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: BankIssuerPaymentDelegate,
) : BaseWebRedirectComposer {
    override val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    @Suppress("ktlint:standard:property-naming")
    override val _uiEvent: MutableSharedFlow<ComposerUiEvent> = MutableSharedFlow()

    override fun cancel() {
        scope.cancel()
    }

    override fun onResultCancelled(params: WebRedirectLauncherParams) {
        scope.launch {
            paymentDelegate.handleError(
                throwable = PaymentMethodCancelledException(paymentMethodType = params.paymentMethodType),
            )
        }
    }

    override fun onResultOk(params: WebRedirectLauncherParams) {
        startPolling(statusUrl = params.statusUrl, paymentMethodType = params.paymentMethodType)
    }

    internal suspend fun startPaymentFlow(inputable: BankIssuerTokenizationInputable) =
        try {
            tokenizationDelegate.tokenize(
                input =
                    BankIssuerTokenizationInputable(
                        paymentMethodType = inputable.paymentMethodType,
                        primerSessionIntent = inputable.primerSessionIntent,
                        bankIssuer = inputable.bankIssuer,
                    ),
            ).flatMap { paymentMethodTokenData ->
                paymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = inputable.primerSessionIntent,
                )
            }.onFailure {
                paymentDelegate.handleError(it)
            }
        } catch (e: CancellationException) {
            paymentDelegate.handleError(
                throwable = PaymentMethodCancelledException(paymentMethodType = inputable.paymentMethodType),
            )
            Result.failure(e)
        }

    internal suspend fun start() {
        paymentDelegate.uiEvent.collectLatest { uiEvent ->
            _uiEvent.emit(uiEvent)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(
        statusUrl: String,
        paymentMethodType: String,
    ) = scope.launch {
        pollingInteractor.execute(
            AsyncStatusParams(statusUrl, paymentMethodType),
        ).mapLatest { status ->
            paymentDelegate.resumePayment(status.resumeToken)
        }.catch {
            paymentDelegate.handleError(it)
        }.collect()
    }
}

object BanksComponentProvider : DISdkComponent {
    fun provideInstance(
        owner: ViewModelStoreOwner,
        paymentMethodType: String,
        onFinished: () -> Unit,
    ): BanksComponent {
        val rpcContainer = RpcContainer { getSdkContainer() }
        getSdkContainer().registerContainer(rpcContainer)
        getSdkContainer().registerContainer(WebRedirectContainer { getSdkContainer() })

        val viewModel =
            ViewModelProvider(
                owner = owner,
                factory =
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(
                            modelClass: Class<T>,
                            extras: CreationExtras,
                        ): T =
                            DefaultBanksComponent(
                                paymentMethodType = paymentMethodType,
                                redirectComposer = resolve(name = paymentMethodType),
                                getBanksDelegate = resolve(),
                                eventLoggingDelegate =
                                    resolve(
                                        name = paymentMethodType,
                                    ),
                                errorLoggingDelegate = resolve(name = paymentMethodType),
                                validationErrorLoggingDelegate = resolve(PaymentMethodType.ADYEN_IDEAL.name),
                                errorMapperRegistry = resolve(),
                                savedStateHandle =
                                    runCatching {
                                        extras.createSavedStateHandle()
                                    }.getOrDefault(SavedStateHandle()),
                                onFinished = onFinished,
                            ) as T
                    },
            )[DefaultBanksComponent::class.java]

        viewModel.addCloseable {
            getSdkContainer().unregisterContainer<RpcContainer>()
        }
        return viewModel
    }
}
