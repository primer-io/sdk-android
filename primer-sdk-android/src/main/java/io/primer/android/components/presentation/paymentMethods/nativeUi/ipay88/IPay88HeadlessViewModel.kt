package io.primer.android.components.presentation.paymentMethods.nativeUi.ipay88

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.State
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.AsyncPaymentMethodConfigInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodParams
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.ui.activity.IPay88ActivityLauncherParams
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.async.webRedirect.WebRedirectPaymentInstrumentParams
import io.primer.android.ui.base.webview.WebViewActivity.Companion.RESULT_ERROR
import io.primer.ipay88.api.ui.NativeIPay88Activity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.get

internal class IPay88HeadlessViewModel(
    private val asyncPaymentMethodConfigInteractor: AsyncPaymentMethodConfigInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle) {

    override val initialState: State = IPay88State.Idle

    override fun initialize(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        initialState: State?
    ) {
        super.initialize(paymentMethodType, sessionIntent, initialState)
        if (initialState is IPay88State.StartRedirect) {
            onEvent(
                IPay88Event.OnRedirect(
                    initialState.paymentMethodType,
                    initialState.statusUrl,
                    initialState.iPayPaymentId,
                    initialState.iPayMethod,
                    initialState.merchantCode,
                    initialState.amount,
                    initialState.referenceNumber,
                    initialState.prodDesc,
                    initialState.currencyCode,
                    initialState.countryCode,
                    initialState.customerName,
                    initialState.customerEmail,
                    initialState.backendCallbackUrl,
                    initialState.deeplinkUrl
                )
            )
        }
    }

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(IPay88Event.OnLoadConfiguration(paymentMethodType))
    }

    @Suppress("LongMethod")
    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: throw IllegalStateException("Invalid transition for event $e")

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is IPaySideEffect.LoadConfiguration &&
                e is IPay88Event.OnLoadConfiguration -> loadConfiguration(
                e.paymentMethodType
            )
            validTransition.sideEffect is IPaySideEffect.Tokenize &&
                e is IPay88Event.OnConfigurationLoaded
            -> tokenize(e.paymentMethodType, e.paymentMethodConfigId, e.locale)
            validTransition.sideEffect is IPaySideEffect.HandleTokenized -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is IPaySideEffect.NavigateToIPayScreen &&
                e is IPay88Event.OnRedirect
            -> {
                openHeadlessScreen(e)
            }
            validTransition.sideEffect is IPaySideEffect.HandleResult &&
                e is BaseEvent.OnResult &&
                validTransition.fromState is IPay88State.Redirect
            -> {
                when (e.resultCode) {
                    Activity.RESULT_CANCELED -> {
                        baseErrorEventResolver.resolve(
                            PaymentMethodCancelledException(
                                validTransition.fromState.paymentMethodType
                            ),
                            ErrorMapperType.PAYMENT_METHODS
                        )
                        onEvent(IPay88Event.OnCancel)
                    }
                    Activity.RESULT_OK -> {
                        onEvent(
                            IPay88Event.OnStartPolling(
                                validTransition.fromState.statusUrl,
                                validTransition.fromState.paymentMethodType
                            )
                        )
                    }
                    RESULT_ERROR -> {
                        baseErrorEventResolver.resolve(
                            e.intent?.getSerializableExtra(NativeIPay88Activity.ERROR_KEY) as
                                Exception,
                            ErrorMapperType.I_PAY88
                        )
                        onEvent(IPay88Event.OnError)
                    }
                }
            }
            validTransition.sideEffect is IPaySideEffect.StartPolling &&
                e is IPay88Event.OnStartPolling
            -> {
                startPolling(e.statusUrl, e.paymentMethodType)
            }
            validTransition.sideEffect is IPaySideEffect.HandleFinished -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is IPaySideEffect.HandleError -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is IPaySideEffect.HandleCancel -> {
                _finishActivityEvent.postValue(Unit)
            }
        }
    }

    private fun loadConfiguration(paymentMethodType: String) = viewModelScope.launch {
        asyncPaymentMethodConfigInteractor.execute(
            AsyncPaymentMethodParams(
                paymentMethodType
            )
        ).catch { onEvent(IPay88Event.OnError) }
            .collect {
                onEvent(
                    IPay88Event.OnConfigurationLoaded(
                        paymentMethodType,
                        it.paymentMethodConfigId,
                        it.locale,
                    )
                )
            }
    }

    private fun tokenize(
        paymentMethodType: String,
        paymentMethodConfigId: String,
        locale: String,
    ) = viewModelScope.launch {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                WebRedirectPaymentInstrumentParams(
                    paymentMethodType,
                    paymentMethodConfigId,
                    locale,
                    asyncPaymentMethodDeeplinkInteractor(None())
                ),
                sessionIntent,
            )
        )
            .catch { onEvent(IPay88Event.OnError) }
            .collect { onEvent(IPay88Event.OnTokenized) }
    }

    private fun startPolling(statusUrl: String, paymentMethodType: String) = viewModelScope.launch {
        asyncPaymentMethodInteractor.execute(
            AsyncMethodParams(statusUrl, paymentMethodType)
        ).catch {
            onEvent(IPay88Event.OnError)
        }.collect { onEvent(IPay88Event.OnFinished) }
    }

    private fun openHeadlessScreen(event: IPay88Event.OnRedirect) {
        _startActivityEvent.postValue(
            IPay88ActivityLauncherParams(
                event.iPayPaymentId,
                event.iPayMethod,
                event.merchantCode,
                event.amount,
                event.referenceNumber,
                event.prodDesc,
                event.currencyCode,
                event.countryCode,
                event.customerName,
                event.customerEmail,
                event.backendCallbackUrl,
                event.deeplinkUrl,
                RESULT_ERROR,
                event.paymentMethodType,
                sessionIntent,
            )
        )
    }

    companion object : DIAppComponent {

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return IPay88HeadlessViewModel(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    extras.createSavedStateHandle()
                ) as T
            }
        }
    }
}
