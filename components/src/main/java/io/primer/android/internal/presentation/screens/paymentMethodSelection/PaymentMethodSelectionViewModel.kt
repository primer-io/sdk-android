package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.core.domain.None
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PaymentMethodSelectionViewModel(
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val checkoutNavigator: CheckoutNavigator,
    private val availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase,
    private val formatAmountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val componentsEventsRepository: ComponentsEventsRepository,
) : ViewModel(), PrimerPaymentMethodSelectionScope {

    private val _uiState = MutableStateFlow(PrimerPaymentMethodSelectionScope.State())
    override val state: StateFlow<PrimerPaymentMethodSelectionScope.State> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        _uiState.value = PrimerPaymentMethodSelectionScope.State(
            paymentMethods = availablePaymentMethodsUseCase.cache,
        )
    }

    private fun formatAmount(amountInCents: Int, currencyCode: String): String {
        val monetaryAmount = MonetaryAmount.create(
            currency = currencyCode,
            value = amountInCents,
        )

        return monetaryAmount?.let {
            formatAmountToCurrencyInteractor.execute(
                FormatCurrencyParams(amount = it),
            )
        } ?: ""
    }

    override fun formatTitleAmount(): String {
        val orderInfo = basicOrderInfoInteractor(None)
        return formatAmount(orderInfo.totalAmount, orderInfo.currencyCode)
    }

    override fun formatSurchargeAmount(amountInCents: Int): String {
        val orderInfo = basicOrderInfoInteractor(None)
        val formattedAmount = formatAmount(amountInCents, orderInfo.currencyCode)
        return if (formattedAmount.isNotEmpty()) {
            "+ $formattedAmount"
        } else {
            ""
        }
    }

    override fun onPaymentMethodSelected(paymentMethod: String) {
        componentsEventsRepository.send(EventType.PAYMENT_METHOD_SELECTION)
        viewModelScope.launch {
            when (paymentMethod) {
                PaymentMethodType.PAYMENT_CARD.name -> {
                    checkoutNavigator.navigateTo(Screen.CardForm)
                }
                PaymentMethodType.GOOGLE_PAY.name,
                PaymentMethodType.PAYPAL.name,
                PaymentMethodType.ADYEN_IDEAL.name -> {
                    checkoutNavigator.navigateTo(Screen.NativeUi(paymentMethod))
                }
            }
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
