package io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
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
import io.primer.android.components.ui.activity.WebRedirectActivityLauncherParams
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
import io.primer.android.ui.base.webview.WebViewClientType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.get

internal class AsyncPaymentMethodNativeUiHeadlessViewModel(
    private val asyncPaymentMethodConfigInteractor: AsyncPaymentMethodConfigInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle) {

    override val initialState: State = AsyncState.Idle

    override fun initialize(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        initialState: State?
    ) {
        super.initialize(paymentMethodType, sessionIntent, initialState)
        if (initialState is AsyncState.StartRedirect) {
            onEvent(
                AsyncEvent.OnRedirect(
                    initialState.title,
                    initialState.paymentMethodType,
                    initialState.redirectUrl,
                    initialState.statusUrl,
                    initialState.returnUrl
                )
            )
        }
    }

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(AsyncEvent.OnLoadConfiguration(paymentMethodType))
    }

    @Suppress("LongMethod")
    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: throw IllegalStateException("Invalid transition for event $e")

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is AsyncSideEffect.LoadConfiguration &&
                e is AsyncEvent.OnLoadConfiguration -> loadConfiguration(
                e.paymentMethodType
            )
            validTransition.sideEffect is AsyncSideEffect.Tokenize &&
                e is AsyncEvent.OnConfigurationLoaded
            -> tokenize(e.paymentMethodType, e.paymentMethodConfigId, e.locale)
            validTransition.sideEffect is AsyncSideEffect.HandleTokenized -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is AsyncSideEffect.NavigateToAsyncScreen &&
                e is AsyncEvent.OnRedirect
            -> {
                openHeadlessScreen(e)
            }
            validTransition.sideEffect is AsyncSideEffect.HandleResult &&
                e is BaseEvent.OnResult &&
                validTransition.fromState is AsyncState.Redirect
            -> {
                when (e.resultCode) {
                    RESULT_CANCELED -> {
                        baseErrorEventResolver.resolve(
                            PaymentMethodCancelledException(
                                validTransition.fromState.paymentMethodType
                            ),
                            ErrorMapperType.PAYMENT_METHODS
                        )
                        onEvent(AsyncEvent.OnCancel)
                    }
                    RESULT_OK -> {
                        onEvent(
                            AsyncEvent.OnStartPolling(
                                validTransition.fromState.statusUrl,
                                validTransition.fromState.paymentMethodType
                            )
                        )
                    }
                }
            }
            validTransition.sideEffect is AsyncSideEffect.StartPolling &&
                e is AsyncEvent.OnStartPolling
            -> {
                startPolling(e.statusUrl, e.paymentMethodType)
            }
            validTransition.sideEffect is AsyncSideEffect.HandleFinished -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is AsyncSideEffect.HandleError -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is AsyncSideEffect.HandleCancel -> {
                _finishActivityEvent.postValue(Unit)
            }
        }
    }

    private fun loadConfiguration(paymentMethodType: String) = viewModelScope.launch {
        asyncPaymentMethodConfigInteractor.execute(
            AsyncPaymentMethodParams(
                paymentMethodType
            )
        ).catch { onEvent(AsyncEvent.OnError) }
            .collect {
                onEvent(
                    AsyncEvent.OnConfigurationLoaded(
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
            .catch { onEvent(AsyncEvent.OnError) }
            .collect { onEvent(AsyncEvent.OnTokenized) }
    }

    private fun startPolling(statusUrl: String, paymentMethodType: String) = viewModelScope.launch {
        asyncPaymentMethodInteractor.execute(
            AsyncMethodParams(statusUrl, paymentMethodType)
        ).catch {
            onEvent(AsyncEvent.OnError)
        }.collect { onEvent(AsyncEvent.OnFinished) }
    }

    private fun openHeadlessScreen(event: AsyncEvent.OnRedirect) {
        _startActivityEvent.postValue(
            WebRedirectActivityLauncherParams(
                event.statusUrl,
                event.redirectUrl,
                event.title,
                event.paymentMethodType,
                event.returnUrl,
                WebViewClientType.ASYNC
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
                return AsyncPaymentMethodNativeUiHeadlessViewModel(
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
