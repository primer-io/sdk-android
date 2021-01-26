package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.payment.PaymentMethodDescriptor
import java.util.*

internal class PrimerViewModel(model: Model) : BaseViewModel(model), EventBus.EventListener {
  private val log = Logger("view-model")
  private lateinit var subscription: EventBus.SubscriptionHandle

  val keyboardVisible = MutableLiveData(false)

//  val sheetDismissed: MutableLiveData<Boolean> = MutableLiveData(false)

  val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

  // Vaulted Payment Methods
  val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodToken>>(Collections.emptyList())

  // Select Payment Method
  val paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(Collections.emptyList())

  val selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)

  val uxMode = MutableLiveData(model.config.uxMode)

  val amount = MutableLiveData(model.config.amount)

//  fun setSheetDismissed(dismissed: Boolean) {
//    sheetDismissed.value = dismissed
//  }

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
          model.configuredPaymentMethods,
          session.paymentMethods
        )

        model.getVaultedPaymentMethods().observe { vault ->
          when (vault) {
            is Observable.ObservableSuccessEvent -> {
              vaultedPaymentMethods.value =
                vault.cast(key = "data", defaultValue = Collections.emptyList())
              paymentMethods.value = resolver.resolve()
              viewStatus.value = getInitialViewStatus()
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

  class ProviderFactory(private val model: Model) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return PrimerViewModel(model) as T
    }
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