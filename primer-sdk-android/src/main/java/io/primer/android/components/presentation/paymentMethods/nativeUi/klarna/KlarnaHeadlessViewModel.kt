package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentCategory
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.State
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentModel
import io.primer.android.components.ui.activity.KlarnaActivityLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentParams
import io.primer.android.klarna.NativeKlarnaActivity
import io.primer.android.ui.base.webview.WebViewActivity.Companion.RESULT_ERROR
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.get

internal class KlarnaHeadlessViewModel(
    private val klarnaSessionInteractor: KlarnaSessionInteractor,
    private val klarnaDeeplinkInteractor: KlarnaDeeplinkInteractor,
    private val klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle), DIAppComponent {

    override val initialState: State = KlarnaState.Idle

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(KlarnaEvent.OnCreateSession)
    }

    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: throw IllegalStateException("Invalid transition")

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is KlarnaSideEffect.CreateSession ->
                createKlarnaPaymentSession()
            validTransition.sideEffect is KlarnaSideEffect.NavigateToKlarna &&
                e is KlarnaEvent.OnSessionCreated
            -> {
                _startActivityEvent.postValue(
                    KlarnaActivityLauncherParams(
                        e.klarnaPaymentData.webViewTitle,
                        e.klarnaPaymentData.clientToken,
                        e.klarnaPaymentData.redirectUrl,
                        e.klarnaPaymentData.paymentCategory,
                        RESULT_ERROR,
                        sessionIntent
                    )
                )
            }
            validTransition.sideEffect is KlarnaSideEffect.HandleResult &&
                e is BaseEvent.OnResult -> {
                when (e.resultCode) {
                    Activity.RESULT_OK -> {
                        val authToken =
                            e.intent?.extras?.getString(NativeKlarnaActivity.AUTH_TOKEN_KEY)
                                .toString()
                        onEvent(KlarnaEvent.OnAuthTokenRetrieved(authToken))
                    }
                    Activity.RESULT_CANCELED -> {
                        baseErrorEventResolver.resolve(
                            PaymentMethodCancelledException(PaymentMethodType.KLARNA.name),
                            ErrorMapperType.DEFAULT
                        )
                        onEvent(KlarnaEvent.OnCancel)
                    }
                    RESULT_ERROR -> {
                        baseErrorEventResolver.resolve(
                            e.intent?.getSerializableExtra(NativeKlarnaActivity.ERROR_KEY) as
                                Exception,
                            ErrorMapperType.KLARNA
                        )
                        onEvent(KlarnaEvent.OnError)
                    }
                }
            }
            validTransition.sideEffect is KlarnaSideEffect.CreateCustomerToken &&
                e is KlarnaEvent.OnAuthTokenRetrieved &&
                validTransition.fromState is KlarnaState.HandlingResult
            -> vaultKlarnaPayment(
                validTransition.fromState.sessionId,
                e.authToken
            )
            validTransition.sideEffect is KlarnaSideEffect.Tokenize &&
                e is KlarnaEvent.OnCustomerTokenRetrieved
            -> tokenize(
                e.customerTokenDataResponse.customerTokenId,
                e.customerTokenDataResponse.sessionData
            )
            validTransition.sideEffect is KlarnaSideEffect.HandleError
            -> _finishActivityEvent.postValue(Unit)
            validTransition.sideEffect is KlarnaSideEffect.HandleCancel
            -> _finishActivityEvent.postValue(Unit)
            validTransition.sideEffect is KlarnaSideEffect.HandleFinished
            -> _finishActivityEvent.postValue(Unit)
        }
    }

    private fun vaultKlarnaPayment(sessionId: String, token: String) {
        viewModelScope.launch {
            klarnaCustomerTokenInteractor.execute(
                KlarnaCustomerTokenParam(
                    sessionId,
                    token
                )
            )
                .catch { onEvent(KlarnaEvent.OnError) }
                .collect { onEvent(KlarnaEvent.OnCustomerTokenRetrieved(it)) }
        }
    }

    private fun tokenize(
        customerTokenId: String?,
        sessionData: CreateCustomerTokenDataResponse.SessionData
    ) = viewModelScope.launch {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                KlarnaPaymentInstrumentParams(
                    customerTokenId,
                    sessionData
                ),
                sessionIntent,
            )
        ).catch { onEvent(KlarnaEvent.OnError) }
            .collect { onEvent(KlarnaEvent.OnTokenized) }
    }

    private fun createKlarnaPaymentSession() = viewModelScope.launch {
        klarnaSessionInteractor.execute(None()).mapLatest { klarnaSession ->
            KlarnaPaymentModel(
                klarnaSession.webViewTitle,
                klarnaDeeplinkInteractor.execute(None()),
                klarnaSession.sessionId,
                klarnaSession.clientToken,
                KlarnaPaymentCategory.PAY_NOW
            )
        }.catch { onEvent(KlarnaEvent.OnError) }
            .collect {
                onEvent(KlarnaEvent.OnSessionCreated(it))
            }
    }

    internal companion object : DIAppComponent {

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return KlarnaHeadlessViewModel(
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
