package io.primer.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.PaymentMethod
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import java.util.Collections

internal class PrimerViewModelFactory(
    private val model: Model,
    private val checkoutConfig: CheckoutConfig,
    private val primerPaymentMethodDescriptorResolver: PrimerPaymentMethodDescriptorResolver,
) : ViewModelAssistedFactory<PrimerViewModel> {

    override fun create(handle: SavedStateHandle): PrimerViewModel =
        PrimerViewModel(model, checkoutConfig, primerPaymentMethodDescriptorResolver)
}

internal class PrimerViewModel constructor(
    private val model: Model,
    private val checkoutConfig: CheckoutConfig,
    private val paymentMethodDescriptorResolver: PrimerPaymentMethodDescriptorResolver,
) : ViewModel(), EventBus.EventListener {

    companion object {

        // FIXME drop this. consider activityViewModels().
        fun getInstance(owner: ViewModelStoreOwner): PrimerViewModel {
            return ViewModelProvider(owner).get(PrimerViewModel::class.java)
        }
    }

    private val log = Logger("view-model")
    private lateinit var subscription: EventBus.SubscriptionHandle

    val keyboardVisible = MutableLiveData(false)

    val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

    val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodTokenInternal>>(
        Collections.emptyList()
    )

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDescriptor?> = _selectedPaymentMethod

    fun selectPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor) {
        _selectedPaymentMethod.value = paymentMethodDescriptor
    }

    // FIXME rename or hook with lifecycle observer
    fun initialize() {
        viewModelScope.launch {
            when (val result = model.getConfiguration()) {
                is OperationResult.Success -> {
                    val clientSession: ClientSession = result.data
                    handleVaultedPaymentMethods(clientSession)
                }
                is OperationResult.Error -> {
                    log("Failed to get configuration: $result")
                }
            }
        }

        subscription = EventBus.subscribe(this)
    }

    private suspend fun handleVaultedPaymentMethods(clientSession: ClientSession) =
        when (val result = model.getVaultedPaymentMethods(clientSession)) {
            is OperationResult.Success -> {
                val paymentModelTokens: List<PaymentMethodTokenInternal> = result.data
                vaultedPaymentMethods.postValue(paymentModelTokens)

                val descriptors =
                    paymentMethodDescriptorResolver.resolve(clientSession)

                _paymentMethods.postValue(descriptors)

                if (this.checkoutConfig.isStandalonePaymentMethod) {
                    _selectedPaymentMethod.postValue(descriptors.first())
                } else {
                    viewStatus.postValue(getInitialViewStatus(paymentModelTokens))
                }
            }
            is OperationResult.Error -> {
                // TODO proper error handling
                log("Failed to get payment methods: ${result.error.message}")
            }
        }

    override fun onCleared() {
        super.onCleared()
        subscription.unregister()
    }

    private fun getInitialViewStatus(
        vaultedPaymentMethods: List<PaymentMethodTokenInternal>,
    ): ViewStatus =
        if (vaultedPaymentMethods.isNotEmpty()) {
            ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
        } else {
            ViewStatus.SELECT_PAYMENT_METHOD
        }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenRemovedFromVault -> {
                vaultedPaymentMethods.value =
                    vaultedPaymentMethods.value?.filter { it.token != e.data.token }
            }
            is CheckoutEvent.TokenAddedToVault -> {
                val hasMatch = vaultedPaymentMethods.value
                    ?.count { it.analyticsId == e.data.analyticsId } ?: 0 > 0
                if (hasMatch) {
                    vaultedPaymentMethods.value = vaultedPaymentMethods.value?.plus(
                        PaymentMethodTokenAdapter.externalToInternal(e.data)
                    )
                }
            }
        }
    }
}
