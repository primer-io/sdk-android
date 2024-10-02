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
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.GooglePayShippingMethodUpdateValidator
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class GooglePayHeadlessViewModel(
    private val configurationInteractor: GooglePayConfigurationInteractor,
    private val googlePayShippingMethodUpdateValidator: GooglePayShippingMethodUpdateValidator,
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

    override fun onEvent(event: Event) {
        val validTransition = stateMachine.transition(event) as? StateMachine.Transition.Valid
            ?: throw IllegalStateException("Invalid transition for event $event")

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is GooglePaySideEffect.OpenHeadlessScreen
            -> _startActivityEvent.postValue(GooglePayActivityLauncherParams())

            validTransition.sideEffect is GooglePaySideEffect.NavigateToGooglePay &&
                event is GooglePayEvent.StartRedirect -> {
                onRedirect(event.activity)
            }

            validTransition.sideEffect is GooglePaySideEffect.HandleResult &&
                event is BaseEvent.OnResult
            -> {
                when (event.resultCode) {
                    Activity.RESULT_OK -> {
                        onEvent(
                            GooglePayEvent.OnTokenizeStart(
                                event.intent?.let {
                                    PaymentData.getFromIntent(it)
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
                        AutoResolveHelper.getStatusFromIntent(event.intent)?.let {
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
                event is GooglePayEvent.OnTokenizeStart -> {
                tokenize(event.paymentData)
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
        configurationInteractor.execute(None()).catch { GooglePayEvent.OnError }.collect { configuration ->
            GooglePayFacadeFactory().create(activity, configuration.environment, logReporter).pay(
                activity,
                configuration.gatewayMerchantId,
                configuration.merchantName,
                configuration.totalPrice,
                configuration.countryCode,
                configuration.currencyCode,
                configuration.allowedCardNetworks,
                configuration.allowedCardAuthMethods,
                configuration.billingAddressRequired,
                configuration.shippingOptions,
                configuration.shippingAddressParameters,
                configuration.requireShippingMethod,
                configuration.emailAddressRequired
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
            }.flatMapLatest {
                configurationInteractor(None()).flatMapLatest { configuration ->
                    handlePaymentDataUpdate(paymentData).flatMapLatest {
                        handleShippingMethodIdUpdate(paymentData).flatMapLatest {
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
                }
            }.catch {
                onEvent(GooglePayEvent.OnError)
            }.collect {
                onEvent(GooglePayEvent.OnTokenized)
            }
        }

    private fun handlePaymentDataUpdate(paymentData: PaymentData?): Flow<Unit> {
        val action = paymentData.mapToMultipleActionUpdateParams()
        return action?.let {
            actionInteractor(action).map { Unit }
        } ?: flowOf(Unit)
    }

    private fun handleShippingMethodIdUpdate(paymentData: PaymentData?): Flow<Unit> {
        val action = paymentData.mapToShippingOptionIdParams()
        when {
            action == null -> return flowOf(Unit)
            else -> return googlePayShippingMethodUpdateValidator(action)
                .flatMapLatest { actionInteractor(action).map { } }
        }
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
                    resolve(),
                    extras.createSavedStateHandle()
                ) as T
            }
        }
    }
}
