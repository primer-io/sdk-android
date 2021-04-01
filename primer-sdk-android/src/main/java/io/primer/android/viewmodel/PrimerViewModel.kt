package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.PaymentMethod
import io.primer.android.UXMode
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.definition.Kind
import java.util.*

@KoinApiExtension // FIXME inject dependencies via ctor
internal class PrimerViewModel(
    // private val model: Model,
    // private val checkoutConfig: CheckoutConfig,
    // private val configuredPaymentMethods: List<PaymentMethod>
) : ViewModel(), EventBus.EventListener, DIAppComponent {

    companion object {

        // FIXME drop this. consider activityViewModels().
        fun getInstance(owner: ViewModelStoreOwner): PrimerViewModel {
            return ViewModelProvider(owner).get(PrimerViewModel::class.java)
        }
    }

    private val log = Logger("view-model")
    private lateinit var subscription: EventBus.SubscriptionHandle

    private val model: Model by inject()
    private val checkoutConfig: CheckoutConfig by inject()
    private val configuredPaymentMethods: List<PaymentMethod> by inject()

    val keyboardVisible = MutableLiveData(false)

    val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

    val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodTokenInternal>>(Collections.emptyList())
    val paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(Collections.emptyList())
    val selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)

    fun setSelectedPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor) {
        selectedPaymentMethod.value = paymentMethodDescriptor
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

        subscription = EventBus.subscribe(this) // FIXME drop eventbus
    }

    private suspend fun handleVaultedPaymentMethods(clientSession: ClientSession) {
        when (val result: OperationResult<List<PaymentMethodTokenInternal>> = model.getVaultedPaymentMethods(clientSession)) {
            is OperationResult.Success -> {
                val paymentModelTokens: List<PaymentMethodTokenInternal> = result.data
                vaultedPaymentMethods.postValue(paymentModelTokens)

                val paymentMethodDescriptorFactory = PaymentMethodDescriptorFactory()

                // FIXME needs to be injected
                val resolver = PaymentMethodDescriptorResolver(
                    checkoutConfig = checkoutConfig,
                    configured = configuredPaymentMethods,
                    paymentMethodRemoteConfigs = clientSession.paymentMethods,
                    paymentMethodDescriptorFactory = paymentMethodDescriptorFactory
                )

                val descriptors = resolver.resolve()
                paymentMethods.postValue(descriptors)

                if (checkoutConfig.uxMode == UXMode.STANDALONE_PAYMENT_METHOD) {
                    selectedPaymentMethod.postValue(descriptors.first())
                } else {
                    viewStatus.postValue(getInitialViewStatus(paymentModelTokens))
                }
            }
            is OperationResult.Error -> {
                // TODO proper error handling
                log("Failed to get payment methods: ${result.error.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscription.unregister()
    }

    private fun getInitialViewStatus(vaultedPaymentMethods: List<PaymentMethodTokenInternal>): ViewStatus {
        if (vaultedPaymentMethods.isNotEmpty()) {
            return ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
        }

        return ViewStatus.SELECT_PAYMENT_METHOD
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenRemovedFromVault -> {
                vaultedPaymentMethods.value =
                    vaultedPaymentMethods.value?.filter { it.token != e.data.token }
            }
            is CheckoutEvent.TokenAddedToVault -> {
                if (vaultedPaymentMethods.value?.find { it.analyticsId == e.data.analyticsId } == null) {
                    vaultedPaymentMethods.value = vaultedPaymentMethods.value?.plus(PaymentMethodTokenAdapter.externalToInternal(e.data))
                }
            }
        }
    }
}
