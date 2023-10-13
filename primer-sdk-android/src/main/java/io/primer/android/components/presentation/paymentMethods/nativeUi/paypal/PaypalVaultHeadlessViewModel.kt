package io.primer.android.components.presentation.paymentMethods.nativeUi.paypal

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
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalConfirmBillingAgreementInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCreateBillingAgreementInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalVaultConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalVaultConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalVaultValidationRulesResolver
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.ui.activity.BrowserLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

internal class PaypalVaultHeadlessViewModel(
    private val configurationInteractor: PaypalVaultConfigurationInteractor,
    private val createBillingAgreementInteractor: PaypalCreateBillingAgreementInteractor,
    private val confirmBillingAgreementInteractor: PaypalConfirmBillingAgreementInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val validationRulesResolver: PaypalVaultValidationRulesResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle), DISdkComponent {

    override val initialState: State = PaypalVaultState.Idle

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(PaypalVaultEvent.OnLoadConfiguration)
    }

    @Suppress("ComplexMethod")
    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: return
        when {
            validTransition.sideEffect is PaypalVaultSideEffect.LoadConfiguration ->
                loadPaymentConfiguration()
            validTransition.sideEffect is PaypalVaultSideEffect.CreateBillingAgreement &&
                e is PaypalVaultEvent.OnCreateBillingAgreement -> createBillingAgreement(
                e.configuration
            )
            validTransition.sideEffect is PaypalVaultSideEffect.NavigateToPaypal &&
                e is PaypalVaultEvent.OnBillingAgreementCreated
            -> {
                _startActivityEvent.postValue(
                    BrowserLauncherParams(
                        e.approvalUrl,
                        Uri.parse(e.successUrl).host.orEmpty(),
                        PaymentMethodType.PAYPAL.name,
                        sessionIntent
                    )
                )
            }
            validTransition.sideEffect is PaypalVaultSideEffect.HandleResult &&
                e is BaseEvent.OnBrowserResult &&
                validTransition.fromState is PaypalVaultState.Redirect
            -> handleBrowserResult(e, validTransition.fromState)
            validTransition.sideEffect is PaypalVaultSideEffect.ConfirmBillingAgreement &&
                e is PaypalVaultEvent.OnConfirmBillingAgreement ->
                confirmBillingAgreement(e.paymentMethodConfigId, e.orderId)
            validTransition.sideEffect is PaypalVaultSideEffect.Tokenize &&
                e is PaypalVaultEvent.OnBillingAgreementConfirmed
            -> tokenize(e.paypalConfirmBillingAgreement)
            validTransition.sideEffect is PaypalVaultSideEffect.HandleError -> {
                _finishActivityEvent.postValue(Unit)
            }
            validTransition.sideEffect is PaypalVaultSideEffect.HandleFinished -> {
                _finishActivityEvent.postValue(Unit)
            }
        }
    }

    private fun loadPaymentConfiguration() = viewModelScope.launch {
        configurationInteractor(None())
            .catch { onEvent(PaypalVaultEvent.OnError) }
            .collect {
                onEvent(PaypalVaultEvent.OnCreateBillingAgreement(it))
            }
    }

    private fun createBillingAgreement(configuration: PaypalVaultConfiguration) =
        viewModelScope.launch {
            createBillingAgreementInteractor(
                PaypalCreateBillingAgreementParams(
                    configuration.paymentMethodConfigId,
                    configuration.successUrl,
                    configuration.cancelUrl
                )
            ).catch { onEvent(PaypalVaultEvent.OnError) }
                .collect {
                    onEvent(
                        PaypalVaultEvent.OnBillingAgreementCreated(
                            it.approvalUrl,
                            it.paymentMethodConfigId,
                            it.successUrl,
                            it.cancelUrl
                        )
                    )
                }
        }

    private fun handleBrowserResult(
        event: BaseEvent.OnBrowserResult,
        state: PaypalVaultState.Redirect
    ) {
        when (event.uri?.buildUpon()?.clearQuery()?.build()) {
            Uri.parse(state.successUrl) -> onEvent(
                PaypalVaultEvent.OnConfirmBillingAgreement(
                    state.paymentMethodConfigId,
                    event.uri?.getQueryParameter(TOKEN_QUERY_PARAM)
                )
            )
            else -> {
                baseErrorEventResolver.resolve(
                    PaymentMethodCancelledException(
                        PaymentMethodType.PAYPAL.name
                    ),
                    ErrorMapperType.DEFAULT
                )
                onEvent(PaypalVaultEvent.OnError)
            }
        }
    }

    private fun confirmBillingAgreement(
        paymentMethodConfigId: String,
        token: String?
    ) = viewModelScope.launch {
        combine(
            validationRulesResolver.resolve().rules.map {
                flowOf(it.validate(token))
            }
        ) { validationResults ->
            validationResults.forEach { result ->
                if (result is ValidationResult.Failure) throw result.exception
            }
        }.catch { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }.flatMapLatest {
            confirmBillingAgreementInteractor(
                PaypalConfirmBillingAgreementParams(
                    paymentMethodConfigId,
                    requireNotNull(token)
                )
            )
        }.catch { onEvent(PaypalVaultEvent.OnError) }
            .collect { onEvent(PaypalVaultEvent.OnBillingAgreementConfirmed(it)) }
    }

    private fun tokenize(
        billingAgreement: PaypalConfirmBillingAgreement
    ) = viewModelScope.launch {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                PaypalVaultPaymentInstrumentParams(
                    billingAgreement.billingAgreementId,
                    billingAgreement.externalPayerInfo,
                    billingAgreement.shippingAddress
                ),
                sessionIntent
            )
        ).catch { onEvent(PaypalVaultEvent.OnError) }
            .collect { onEvent(PaypalVaultEvent.OnTokenized) }
    }

    internal companion object : DISdkComponent {
        private const val TOKEN_QUERY_PARAM = "ba_token"

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return PaypalVaultHeadlessViewModel(
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
