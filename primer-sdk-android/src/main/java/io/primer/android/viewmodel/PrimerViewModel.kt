package io.primer.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.Primer
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import java.util.Collections

@KoinApiExtension
@ExperimentalCoroutinesApi
internal class PrimerViewModel(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel(), EventBus.EventListener {

    private lateinit var subscription: EventBus.SubscriptionHandle

    private val _selectedPaymentMethodId = MutableLiveData("")

    private val _keyboardVisible = MutableLiveData(false)
    val keyboardVisible: LiveData<Boolean> = _keyboardVisible

    val viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.INITIALIZING)

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
                println(e.data.description)
                Primer.instance.dismiss()
            }
        }
    }
}
