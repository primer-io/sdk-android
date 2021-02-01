package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckout
import io.primer.android.di.DIAppComponent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.payment.PaymentMethodDescriptor
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
internal class PrimerViewModel : BaseViewModel(), EventBus.EventListener, DIAppComponent {
  private val log = Logger("view-model")
  private lateinit var subscription: EventBus.SubscriptionHandle

  private val model: Model by inject()
  private val checkoutConfig: CheckoutConfig by inject()
  private val configuredPaymentMethods: List<PaymentMethod> by inject()

  val keyboardVisible = MutableLiveData(false)

  val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

  // Vaulted Payment Methods
  val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodToken>>(Collections.emptyList())

  // Select Payment Method
  val paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(Collections.emptyList())

  val selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)

  fun setSelectedPaymentMethod(pm: PaymentMethodDescriptor) {
    selectedPaymentMethod.value = pm
  }

  override fun initialize() {
    // TODO: clean this shit up
    model.getConfiguration().observe { config ->
      if (config is Observable.ObservableSuccessEvent) {
        val session: ClientSession = config.cast()
        val resolver = PaymentMethodDescriptorResolver(
          this,
          configuredPaymentMethods,
          session.paymentMethods
        )

        model.getVaultedPaymentMethods().observe { vault ->
          when (vault) {
            is Observable.ObservableSuccessEvent -> {
              vaultedPaymentMethods.value =
                vault.cast(key = "data", defaultValue = Collections.emptyList())

              val descriptors = resolver.resolve()

              paymentMethods.value = descriptors

              if (checkoutConfig.uxMode == UniversalCheckout.UXMode.STANDALONE_PAYMENT_METHOD) {
                selectedPaymentMethod.value = descriptors.first()
              } else {
                viewStatus.value = getInitialViewStatus()
              }
            }
            is Observable.ObservableErrorEvent -> log("Failed to get payment methods " + vault.error.description)
          }
        }
      }
    }

    subscription = EventBus.subscribe(this)
  }

  override fun onCleared() {
    super.onCleared()
    subscription.unregister()
  }

  private fun getInitialViewStatus(): ViewStatus {
    if (vaultedPaymentMethods.value?.isNotEmpty() == true) {
      return ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
    }

    return ViewStatus.SELECT_PAYMENT_METHOD
  }

  companion object {
    fun getInstance(owner: ViewModelStoreOwner): PrimerViewModel {
      return ViewModelProvider(owner).get(PrimerViewModel::class.java)
    }
  }

  override fun onEvent(e: CheckoutEvent) {
    when (e) {
      is CheckoutEvent.TokenRemovedFromVault -> {
        vaultedPaymentMethods.value =
          vaultedPaymentMethods.value?.filter { it.token != e.data.token }
      }
      is CheckoutEvent.TokenAddedToVault -> {
        if (vaultedPaymentMethods.value?.find { it.analyticsId == e.data.analyticsId } == null) {
          vaultedPaymentMethods.value = vaultedPaymentMethods.value?.plus(e.data)
        }
      }
    }
  }
}