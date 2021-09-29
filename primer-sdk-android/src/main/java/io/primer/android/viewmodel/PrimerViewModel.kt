package io.primer.android.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.PaymentMethod
import io.primer.android.Primer
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.DefaultLogger
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethod
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodDescriptorMapping
import io.primer.android.payment.DefaultPaymentMethodMapping
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.payment.card.Card
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.payment.VaultCapability
import io.primer.android.payment.apaya.Apaya
import kotlinx.coroutines.launch
import java.util.Collections

internal class PrimerViewModel(
    private val application: Application,
    private val model: Model,
    private val config: PrimerConfig,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel(), EventBus.EventListener {

    private val log = DefaultLogger("view-model")
    private lateinit var subscription: EventBus.SubscriptionHandle

    private var _selectedPaymentMethodId = MutableLiveData("")
    var selectedPaymentMethodId: LiveData<String> = _selectedPaymentMethodId

    val keyboardVisible = MutableLiveData(false)

    val viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.INITIALIZING)

    val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodTokenInternal>>(
        Collections.emptyList()
    )

    private val _primerViewModelSetupException = MutableLiveData<Exception>()
    val primerViewModelSetupException: LiveData<Exception> = _primerViewModelSetupException

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDescriptor?> = _selectedPaymentMethod

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

    fun fetchConfiguration() {
        viewModelScope.launch {
            when (val result = model.getConfiguration()) {
                is OperationResult.Success -> {
                    val clientSession: ClientSession = result.data
                    val mapping = DefaultPaymentMethodMapping(config.settings)
                    val factory = PaymentMethodListFactory(mapping)
                    val paymentMethods = initializePaymentMethodModules(clientSession, factory)
                    handleVaultedPaymentMethods(clientSession, paymentMethods)
                }
                is OperationResult.Error -> {
                    log("Failed to get configuration: $result")
                }
            }
        }

        subscription = EventBus.subscribe(this)
    }

    fun setSelectedPaymentMethodId(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String =
        _selectedPaymentMethodId.value ?: ""

    private fun initializePaymentMethodModules(
        clientSession: ClientSession,
        factory: PaymentMethodListFactory,
    ): MutableList<PaymentMethod> {

        val paymentMethods = factory.buildWith(clientSession.paymentMethods)

        if (config.isStandalonePaymentMethod) {
            paymentMethods.find { p ->
                val matches: Boolean = when (config.intent.paymentMethod) {
                    PrimerPaymentMethod.KLARNA -> p is Klarna
                    PrimerPaymentMethod.GOOGLE_PAY -> p is GooglePay
                    PrimerPaymentMethod.PAYPAL -> p is PayPal
                    PrimerPaymentMethod.CARD -> p is Card
                    PrimerPaymentMethod.GOCARDLESS -> p is GoCardless
                    PrimerPaymentMethod.APAYA -> p is Apaya
                    else -> false
                }
                matches
            }?.let { paymentMethod ->
                if (config.paymentMethodIntent.isNotVault ||
                    (config.paymentMethodIntent.isVault && paymentMethod.canBeVaulted)
                ) {
                    paymentMethod.module.initialize(application, clientSession)
                    paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
                    paymentMethod.module.registerPaymentMethodDescriptorFactory(
                        paymentMethodDescriptorFactoryRegistry
                    )
                }
            }
        } else {
            paymentMethods.forEach { paymentMethod ->
                if (config.paymentMethodIntent.isNotVault ||
                    (config.paymentMethodIntent.isVault && paymentMethod.canBeVaulted)
                ) {
                    paymentMethod.module.initialize(application, clientSession)
                    paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
                    paymentMethod.module.registerPaymentMethodDescriptorFactory(
                        paymentMethodDescriptorFactoryRegistry
                    )
                }
            }
        }

        return paymentMethods
    }

    private fun handleError(description: String) {
        val error = APIError(description)
        val event = CheckoutEvent.ApiError(error)
        EventBus.broadcast(event)
    }

    private suspend fun handleVaultedPaymentMethods(
        clientSession: ClientSession,
        configuredPaymentMethods: MutableList<PaymentMethod>,
    ) = when (val result = model.getVaultedPaymentMethods(clientSession)) {
        is OperationResult.Success -> {
            val paymentModelTokens: List<PaymentMethodTokenInternal> = result.data.filter {
                DISALLOWED_PAYMENT_METHOD_TYPES.contains(it.paymentInstrumentType).not()
            }

            vaultedPaymentMethods.postValue(paymentModelTokens)

            if (getSelectedPaymentMethodId().isEmpty() && paymentModelTokens.isNotEmpty()) {
                setSelectedPaymentMethodId(paymentModelTokens[0].token)
            }

            val paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
                localConfig = config,
                localPaymentMethods = configuredPaymentMethods,
                paymentMethodDescriptorFactoryRegistry = paymentMethodDescriptorFactoryRegistry,
                availabilityCheckers = paymentMethodCheckerRegistry
            )

            val descriptors: List<PaymentMethodDescriptor> =
                paymentMethodDescriptorResolver.resolve(clientSession)
                    .filter { descriptor ->
                        isValidPaymentDescriptor(descriptor)
                    }

            _paymentMethods.postValue(descriptors)

            val mapping = PaymentMethodDescriptorMapping(descriptors)

            if (config.isStandalonePaymentMethod) {
                val paymentMethod = config.intent.paymentMethod
                val descriptor = mapping.getDescriptorFor(paymentMethod)

                if (descriptor != null) {
                    _selectedPaymentMethod.postValue(descriptor)
                } else {
                    val description = """
                            |Failed to initialise due to missing configuration. Please ensure the 
                            |requested payment method has been configured in Primer's dashboard.
                        """.trimMargin()
                    handleError(description)
                }
            } else {
                viewStatus.postValue(getInitialViewStatus(paymentModelTokens))
            }
        }
        is OperationResult.Error -> {
            val description = """
                    |Failed to initialise due to a failed network call. Please ensure 
                    |your internet connection is stable and try again.
                """.trimMargin()
            handleError(description)
            log("Failed to get payment methods: ${result.error.message}")
        }
    }

    private fun getInitialViewStatus(
        vaultedPaymentMethods: List<PaymentMethodTokenInternal>,
    ): ViewStatus =
        ViewStatus.SELECT_PAYMENT_METHOD

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
            is CheckoutEvent.ApiError -> {
                println(e.data.description)
                Primer.instance.dismiss()
            }
        }
    }

    private fun isValidPaymentDescriptor(descriptor: PaymentMethodDescriptor) = (
        descriptor.vaultCapability == VaultCapability.VAULT_ONLY &&
            config.paymentMethodIntent.isVault
        ) ||
        descriptor.vaultCapability == VaultCapability.SINGLE_USE_AND_VAULT

    companion object {

        private val DISALLOWED_PAYMENT_METHOD_TYPES = listOf("APAYA")
    }
}
