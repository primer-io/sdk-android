package io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.State
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.GooglePayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation.GooglePayValidationRulesResolver
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.ui.activity.GooglePayActivityLauncherParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.GooglePayException
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.googlepay.GooglePayFlow
import io.primer.android.domain.tokenization.models.paymentInstruments.googlepay.GooglePayPaymentInstrumentParams
import io.primer.android.payment.google.GooglePayFacadeFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

internal class GooglePayHeadlessViewModel(
    private val configurationInteractor: GooglePayConfigurationInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val actionInteractor: ActionInteractor,
    private val validationRulesResolver: GooglePayValidationRulesResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val logReporter: LogReporter,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle) {

    override val initialState: State = GooglePayState.Idle

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(GooglePayEvent.OnOpenHeadlessScreen)
    }

    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: throw IllegalStateException("Invalid transition for event $e")

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is GooglePaySideEffect.OpenHeadlessScreen
            -> _startActivityEvent.postValue(GooglePayActivityLauncherParams())

            validTransition.sideEffect is GooglePaySideEffect.NavigateToGooglePay &&
                e is GooglePayEvent.StartRedirect -> {
                onRedirect(e.activity)
            }
            validTransition.sideEffect is GooglePaySideEffect.HandleResult &&
                e is BaseEvent.OnResult
            -> {
                when (e.resultCode) {
                    Activity.RESULT_OK -> {
                        onEvent(
                            GooglePayEvent.OnTokenizeStart(
                                e.intent?.let {
                                    PaymentData.getFromIntent(
                                        it
                                    )
                                }
                            )
                        )
                    }
                    Activity.RESULT_CANCELED -> {
                        baseErrorEventResolver.resolve(
                            PaymentMethodCancelledException(PaymentMethodType.GOOGLE_PAY.name),
                            ErrorMapperType.DEFAULT
                        )
                        onEvent(GooglePayEvent.OnCancel)
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(e.intent)?.let {
                            baseErrorEventResolver.resolve(
                                GooglePayException(it),
                                ErrorMapperType.GOOGLE_PAY
                            )
                        }
                        onEvent(GooglePayEvent.OnError)
                    }
                }
            }
            validTransition.sideEffect is GooglePaySideEffect.Tokenize &&
                e is GooglePayEvent.OnTokenizeStart -> {
                tokenize(e.paymentData)
            }
            validTransition.sideEffect is GooglePaySideEffect.HandleCancel -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is GooglePaySideEffect.HandleError -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is GooglePaySideEffect.HandleFinished -> {
                _finishActivityEvent.postValue(Unit)
            }
        }
    }

    private fun onRedirect(activity: Activity) = viewModelScope.launch {
        configurationInteractor.execute(None()).catch { GooglePayEvent.OnError }.collect {
            GooglePayFacadeFactory().create(activity, it.environment, logReporter).pay(
                activity,
                it.gatewayMerchantId,
                it.merchantName,
                it.totalPrice,
                it.countryCode,
                it.currencyCode,
                it.allowedCardNetworks,
                it.allowedCardAuthMethods,
                it.billingAddressRequired
            )
        }
    }

    private fun tokenize(paymentData: PaymentData?) =
        viewModelScope.launch {
            combine(
                validationRulesResolver.resolve().rules.map {
                    flowOf(it.validate(paymentData))
                }
            ) { validationResults ->
                validationResults.forEach { result ->
                    if (result is ValidationResult.Failure) throw result.exception
                }
            }.catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
            }
                .flatMapLatest {
                    configurationInteractor.execute(None()).flatMapLatest { configuration ->
                        handleBillingAddressCapture(paymentData).flatMapLatest {
                            tokenizationInteractor.executeV2(
                                TokenizationParamsV2(
                                    GooglePayPaymentInstrumentParams(
                                        PaymentMethodType.GOOGLE_PAY.name,
                                        configuration.gatewayMerchantId,
                                        requireNotNull(paymentData),
                                        GooglePayFlow.GATEWAY
                                    ),
                                    PrimerSessionIntent.CHECKOUT
                                )
                            )
                        }
                    }
                }.catch { onEvent(GooglePayEvent.OnError) }.collect {
                    onEvent(GooglePayEvent.OnTokenized)
                }
        }

    private fun handleBillingAddressCapture(
        paymentData: PaymentData?
    ): Flow<Unit> {
        val googlePayBillingAddressMapper = GooglePayBillingAddressMapper()
        val action = googlePayBillingAddressMapper.mapToClientSessionUpdateParams(paymentData)
        return action?.let {
            actionInteractor(action).catch { emit(Unit) }
        } ?: flowOf(Unit)
    }

    companion object : DISdkComponent {
        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return GooglePayHeadlessViewModel(
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    extras.createSavedStateHandle()
                ) as T
            }
        }
    }
}
