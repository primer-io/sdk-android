package io.primer.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.PaymentMethod
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
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import kotlinx.coroutines.launch
import java.util.Collections

internal class PrimerViewModelFactory(
    private val model: Model,
    private val checkoutConfig: CheckoutConfig,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val primerPaymentMethodDescriptorResolver: PrimerPaymentMethodDescriptorResolver,
) : AndroidViewModelAssistedFactory<PrimerViewModel> {

    override fun create(application: Application, handle: SavedStateHandle): PrimerViewModel =
        PrimerViewModel(
            application,
            model,
            checkoutConfig,
            paymentMethodCheckerRegistry,
            paymentMethodDescriptorFactoryRegistry,
            primerPaymentMethodDescriptorResolver
        )
}

internal class PrimerViewModel(
    application: Application,
    private val model: Model,
    private val checkoutConfig: CheckoutConfig,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val paymentMethodDescriptorResolver: PrimerPaymentMethodDescriptorResolver,
) : AndroidViewModel(application), EventBus.EventListener {

    companion object {

        // FIXME drop this. consider activityViewModels().
        fun getInstance(owner: ViewModelStoreOwner): PrimerViewModel {
            return ViewModelProvider(owner).get(PrimerViewModel::class.java)
        }
    }

    private val log = Logger("view-model")
    private lateinit var subscription: EventBus.SubscriptionHandle

    private var selectedPaymentMethodId = MutableLiveData<String>("")

    val keyboardVisible = MutableLiveData(false)

    val viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.INITIALIZING)

    val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodTokenInternal>>(
        Collections.emptyList()
    )

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDescriptor?> = _selectedPaymentMethod

    fun goToVaultedPaymentMethodsView() {
        viewStatus.value = ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
    }

    fun goToSelectPaymentMethodsView() {
        viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
    }

    fun selectPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor) {
        _selectedPaymentMethod.value = paymentMethodDescriptor
    }

    fun fetchConfiguration(locallyConfiguredPaymentMethods: List<PaymentMethod>) {
        viewModelScope.launch {
            when (val result = model.getConfiguration()) {
                is OperationResult.Success -> {
                    val clientSession: ClientSession = result.data

                    initializePaymentMethodModules(locallyConfiguredPaymentMethods, clientSession)

                    handleVaultedPaymentMethods(clientSession)
                }
                is OperationResult.Error -> {
                    log("Failed to get configuration: $result")
                }
            }
        }

        subscription = EventBus.subscribe(this)
    }

    fun setSelectedPaymentMethodId(id: String) {
        selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String {
        return selectedPaymentMethodId.value ?: ""
    }

    private fun initializePaymentMethodModules(
        locallyConfiguredPaymentMethods: List<PaymentMethod>,
        clientSession: ClientSession,
    ) {
        locallyConfiguredPaymentMethods.forEach { paymentMethod ->
            paymentMethod.module.initialize(getApplication(), clientSession)
            paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
            paymentMethod.module.registerPaymentMethodDescriptorFactory(
                paymentMethodDescriptorFactoryRegistry
            )
        }
    }

    private suspend fun handleVaultedPaymentMethods(clientSession: ClientSession) =
        when (val result = model.getVaultedPaymentMethods(clientSession)) {
            is OperationResult.Success -> {
                val paymentModelTokens: List<PaymentMethodTokenInternal> = result.data

                vaultedPaymentMethods.postValue(paymentModelTokens)

                if (getSelectedPaymentMethodId().isEmpty() && paymentModelTokens.isNotEmpty()) {
                    setSelectedPaymentMethodId(paymentModelTokens[0].token)
                }

                val descriptors: List<PaymentMethodDescriptor> =
                    paymentMethodDescriptorResolver.resolve(clientSession)

                _paymentMethods.postValue(descriptors)

                if (checkoutConfig.isStandalonePaymentMethod) {
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

    private fun getInitialViewStatus(
        vaultedPaymentMethods: List<PaymentMethodTokenInternal>,
    ): ViewStatus = ViewStatus.SELECT_PAYMENT_METHOD

    override fun onCleared() {
        super.onCleared()
        subscription.unregister()
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
