package io.primer.android.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.Primer
import io.primer.android.SessionState
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.ui.AmountLabelContentFactory
import io.primer.android.ui.PaymentMethodButtonGroupFactory
import io.primer.android.utils.SurchargeFormatter
import java.util.Collections
import java.util.Currency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@ExperimentalCoroutinesApi
internal class PrimerViewModel(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor,
    private val actionInteractor: ActionInteractor,
    private val config: PrimerConfig,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel(), EventBus.EventListener {

    private lateinit var subscription: EventBus.SubscriptionHandle

    private val _selectedPaymentMethodId = MutableLiveData("")

    private val _keyboardVisible = MutableLiveData(false)
    val keyboardVisible: LiveData<Boolean> = _keyboardVisible

    val viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.INITIALIZING)

    private val _state = MutableLiveData(SessionState.AWAITING_USER)
    val state: LiveData<SessionState> = _state

    fun setState(s: SessionState) {
        _state.postValue(s)
    }

    val selectedSavedPaymentMethod: PaymentMethodTokenInternal?
        get() = vaultedPaymentMethods.value?.find { it.token == _selectedPaymentMethodId.value }

    val shouldDisplaySavedPaymentMethod: Boolean
        get() = config.intent.paymentMethodIntent.isNotVault && selectedSavedPaymentMethod != null

    private val _vaultedPaymentMethods = MutableLiveData<List<PaymentMethodTokenInternal>>(
        Collections.emptyList()
    )
    val vaultedPaymentMethods: LiveData<List<PaymentMethodTokenInternal>> = _vaultedPaymentMethods

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDescriptor?> = _selectedPaymentMethod

    private val _selectedPaymentMethodBehaviour =
        MutableLiveData<SelectedPaymentMethodBehaviour?>(null)
    val selectedPaymentMethodBehaviour: LiveData<SelectedPaymentMethodBehaviour?> =
        _selectedPaymentMethodBehaviour

    private val _checkoutEvent = MutableLiveData<CheckoutEvent>()
    val checkoutEvent: LiveData<CheckoutEvent> = _checkoutEvent

    fun goToVaultedPaymentMethodsView() {
        viewStatus.postValue(ViewStatus.VIEW_VAULTED_PAYMENT_METHODS)
    }

    fun goToSelectPaymentMethodsView() {
        reselectSavedPaymentMethod()
        viewStatus.postValue(ViewStatus.SELECT_PAYMENT_METHOD)
    }

    fun selectPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor) {
        _selectedPaymentMethod.value = paymentMethodDescriptor
    }

    fun setCheckoutEvent(event: CheckoutEvent) {
        _checkoutEvent.postValue(event)
    }

    fun setKeyboardVisibility(visible: Boolean) {
        _keyboardVisible.postValue(visible)
    }

    fun executeBehaviour(behaviour: SelectedPaymentMethodBehaviour) {
        _selectedPaymentMethodBehaviour.value = behaviour
    }

    fun fetchConfiguration() {
        viewModelScope.launch {
            configurationInteractor(ConfigurationParams(false)).flatMapLatest {
                paymentMethodModulesInteractor(None()).zip(
                    vaultedPaymentMethodsInteractor(None())
                ) { descriptorsHolder, paymentModelTokens ->
                    _vaultedPaymentMethods.postValue(paymentModelTokens)
                    if (getSelectedPaymentMethodId().isEmpty() &&
                        paymentModelTokens.isNotEmpty()
                    ) {
                        setSelectedPaymentMethodId(paymentModelTokens.first().token)

                        // dispatch action
                        val selectedToken = paymentModelTokens.first()
                        val type = selectedToken.surchargeType
                        val network = selectedToken.paymentInstrumentData?.binData?.network
                        val action = ClientSessionActionsRequest.SetPaymentMethod(type, network)
                        dispatchAction(action)
                    }

                    _paymentMethods.postValue(descriptorsHolder.descriptors)
                    descriptorsHolder.selectedPaymentMethodDescriptor?.let {
                        _selectedPaymentMethod.postValue(
                            descriptorsHolder.selectedPaymentMethodDescriptor
                        )
                    } ?: run {
                        viewStatus.postValue(ViewStatus.SELECT_PAYMENT_METHOD)
                    }
                }
            }.collect { }
        }

        subscription = EventBus.subscribe(this)
    }

    fun dispatchAction(
        action: ClientSessionActionsRequest.Action,
        resetState: Boolean = true,
        completion: ((Error?) -> Unit) = {},
    ) {
        setState(SessionState.AWAITING_APP)
        val request = ClientSessionActionsRequest(listOf(action))
        actionInteractor.dispatch(request) { error ->
            // todo: hack to prevent flicker when pushing new view, fix.
            if (error == null && resetState) setState(SessionState.AWAITING_USER)
            else setState(SessionState.ERROR)
            completion(error)
        }
    }

    fun reselectSavedPaymentMethod() {
        val list = vaultedPaymentMethods.value ?: return
        val token = list.find { p -> p.token == getSelectedPaymentMethodId() } ?: return
        var type = token.paymentInstrumentType
        if (token.paymentInstrumentType == "PAYPAL_BILLING_AGREEMENT") type = "PAYPAL"
        val network = token.paymentInstrumentData?.network
        val action = ClientSessionActionsRequest.SetPaymentMethod(type, network)
        dispatchAction(action)
    }

    val paymentMethodButtonGroupFactory: PaymentMethodButtonGroupFactory
        get() = PaymentMethodButtonGroupFactory(actionInteractor.surcharges, formatter)

    private val currency: Currency
        get() = Currency.getInstance(config.monetaryAmount?.currency)

    val surchargeDisabled: Boolean get() {
        if (config.intent.paymentMethodIntent.isVault) return true
        if (actionInteractor.surchargeDataEmptyOrZero) return true
        return false
    }

    private val formatter: SurchargeFormatter
        get() = SurchargeFormatter(actionInteractor.surcharges, currency)

    private val token: PaymentMethodTokenInternal?
        get() = _selectedPaymentMethodId.value
            ?.let { id -> _vaultedPaymentMethods.value?.find { it.token == id } }

    private val savedPaymentMethodSurcharge: Int
        get() = formatter.getSurchargeForSavedPaymentMethod(token)

    fun savedPaymentMethodSurchargeLabel(context: Context): String = formatter
        .getSurchargeLabelTextForPaymentMethodType(savedPaymentMethodSurcharge, context)

    fun amountLabelMonetaryAmount(
        config: PrimerConfig
    ): MonetaryAmount? = AmountLabelContentFactory.build(config, savedPaymentMethodSurcharge)

    val monetaryAmount: MonetaryAmount? get() = config.monetaryAmount

    fun findSurchargeAmount(
        type: String,
        network: String? = null
    ): Int = formatter.getSurchargeForPaymentMethodType(type, network)

    fun setSelectedPaymentMethodId(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String = _selectedPaymentMethodId.value.orEmpty()

    override fun onCleared() {
        super.onCleared()
        subscription.unregister()
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenRemovedFromVault -> {
                _vaultedPaymentMethods.value =
                    vaultedPaymentMethods.value?.filter { it.token != e.data.token }
            }
            is CheckoutEvent.TokenAddedToVault -> {
                val hasMatch = vaultedPaymentMethods.value
                    ?.count { it.analyticsId == e.data.analyticsId } ?: 0 > 0
                if (hasMatch) {
                    _vaultedPaymentMethods.value = vaultedPaymentMethods.value?.plus(
                        PaymentMethodTokenAdapter.externalToInternal(e.data)
                    )
                }
            }
            is CheckoutEvent.ApiError -> {
                Primer.instance.dismiss()
            }
        }
    }
}
