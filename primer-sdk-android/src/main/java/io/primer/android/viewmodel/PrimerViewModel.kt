package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.api.Observable
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodToken
import io.primer.android.session.ClientSession
import java.util.*

internal class PrimerViewModel(model: Model): BaseViewModel(model) {
  private val log = Logger("view-model")

  val sheetDismissed: MutableLiveData<Boolean> = MutableLiveData(false)

  val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

  // Vaulted Payment Methods
  val vaultedPaymentMethods = MutableLiveData<List<PaymentMethodToken>>(Collections.emptyList())

  // Select Payment Method
  val paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(Collections.emptyList())

  val selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)

  val uxMode = MutableLiveData(model.config.uxMode)

  val amount = MutableLiveData(model.config.amount)

  fun setSheetDismissed(dismissed: Boolean) {
    sheetDismissed.value = dismissed
  }

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
              log("GOT PAYMENT METHODS! " + vault.data.toString())
              vaultedPaymentMethods.value = vault.cast(key = "data", defaultValue = Collections.emptyList())
              paymentMethods.value = resolver.resolve()
              viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
            }
            is Observable.ObservableErrorEvent -> log("Failed to get payment methods " + vault.error.description)
            is Observable.ObservableLoadingEvent -> log("Loading payment methods...")
          }
        }
      }
    }
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
}