package io.primer.android.components.presentation.paymentMethods.nativeUi.apaya

import android.app.Activity
import android.net.Uri
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
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaSessionConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaTokenizationConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaSessionConfiguration
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.ui.activity.ApayaActivityLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.ApayaException
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.apaya.ApayaPaymentInstrumentParams
import io.primer.android.ui.base.webview.WebViewActivity.Companion.RESULT_ERROR
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.get

internal class ApayaHeadlessViewModel(
    private val apayaSessionConfigurationInteractor: ApayaSessionConfigurationInteractor,
    private val apayaTokenizationConfigurationInteractor: ApayaTokenizationConfigurationInteractor,
    private val apayaSessionInteractor: ApayaSessionInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle) {

    override val initialState: State = ApayaState.Idle

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(ApayaEvent.OnLoadConfiguration)
    }

    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: return

        currentState = validTransition.toState

        // Invoke side effect to run
        when {
            validTransition.sideEffect is ApayaSideEffect.LoadConfiguration -> loadConfiguration()
            validTransition.sideEffect is ApayaSideEffect.CreateSession &&
                e is ApayaEvent.OnCreateSession -> createApayaSession(e.apayaConfiguration)
            validTransition.sideEffect is ApayaSideEffect.NavigateToApaya &&
                e is ApayaEvent.OnSessionCreated
            -> _startActivityEvent.postValue(
                ApayaActivityLauncherParams(
                    e.apayaPaymentData.webViewTitle.orEmpty(),
                    e.apayaPaymentData.redirectUrl,
                    e.apayaPaymentData.returnUrl,
                    sessionIntent
                )
            )
            validTransition.sideEffect is ApayaSideEffect.HandleResult && e is BaseEvent.OnResult
            -> handleResult(e)
            validTransition.sideEffect is ApayaSideEffect.Tokenize &&
                e is ApayaEvent.OnRedirectUrlRetrieved -> tokenize(e.redirectUrl)
            validTransition.sideEffect is ApayaSideEffect.HandleError
            -> _finishActivityEvent.postValue(Unit)
            validTransition.sideEffect is ApayaSideEffect.HandleCancel
            -> _finishActivityEvent.postValue(Unit)
            validTransition.sideEffect is ApayaSideEffect.HandleFinished
            -> _finishActivityEvent.postValue(Unit)
        }
    }

    private fun loadConfiguration() =
        viewModelScope.launch {
            apayaSessionConfigurationInteractor(None())
                .catch { onEvent(ApayaEvent.OnError) }
                .collect {
                    onEvent(ApayaEvent.OnCreateSession(it))
                }
        }

    private fun handleResult(event: BaseEvent.OnResult) {
        when (event.resultCode) {
            Activity.RESULT_OK -> event.intent?.let {
                onEvent(ApayaEvent.OnRedirectUrlRetrieved(it.data.toString()))
            }
            Activity.RESULT_CANCELED -> {
                baseErrorEventResolver.resolve(
                    PaymentMethodCancelledException(PaymentMethodType.APAYA.name),
                    ErrorMapperType.DEFAULT
                )
                onEvent(ApayaEvent.OnCancel)
            }
            RESULT_ERROR -> {
                event.intent?.let {
                    baseErrorEventResolver.resolve(
                        ApayaException(
                            Uri.parse(it.data?.toString()).getQueryParameter(STATUS_QUERY_KEY)
                        ),
                        ErrorMapperType.APAYA
                    )
                }
                onEvent(ApayaEvent.OnError)
            }
        }
    }

    private fun tokenize(
        redirectUrl: String?
    ) = viewModelScope.launch {
        val params = ApayaWebResultParams(Uri.parse(redirectUrl))
        apayaSessionInteractor.validateWebResultParams(
            ApayaWebResultParams(Uri.parse(redirectUrl))
        ).flatMapLatest {
            apayaTokenizationConfigurationInteractor.execute(None())
                .flatMapLatest { configuration ->
                    tokenizationInteractor.executeV2(
                        TokenizationParamsV2(
                            ApayaPaymentInstrumentParams(
                                params.mxNumber,
                                params.mnc,
                                params.mcc,
                                params.hashedIdentifier,
                                configuration.merchantId,
                                configuration.currencyCode
                            ),
                            sessionIntent,
                        )
                    )
                }
        }.catch { onEvent(ApayaEvent.OnError) }
            .collect { onEvent(ApayaEvent.OnTokenized) }
    }

    private fun createApayaSession(configuration: ApayaSessionConfiguration) =
        viewModelScope.launch {
            apayaSessionInteractor.execute(
                ApayaSessionParams(
                    configuration.merchantAccountId,
                    configuration.locale,
                    configuration.currencyCode,
                    configuration.phoneNumber
                )
            ).catch { ApayaEvent.OnError }
                .collect {
                    onEvent(ApayaEvent.OnSessionCreated(it))
                }
        }

    internal companion object : DIAppComponent {

        private const val STATUS_QUERY_KEY = "status"

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return ApayaHeadlessViewModel(
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
