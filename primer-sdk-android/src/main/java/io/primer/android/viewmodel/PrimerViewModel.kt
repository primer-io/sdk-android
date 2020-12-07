package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.api.Observable
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.session.ClientSession
import java.util.*

internal class PrimerViewModel(model: Model): BaseViewModel(model) {
  private val log = Logger("view-model")

  val sheetDismissed: MutableLiveData<Boolean> = MutableLiveData(false)

  val viewStatus: MutableLiveData<ViewStatus> = MutableLiveData(ViewStatus.INITIALIZING)

  // Select Payment Method
  val paymentMethods = MutableLiveData(Collections.emptyList<PaymentMethodDescriptor>())

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
    model.getConfiguration().observe {
      if (it is Observable.ObservableSuccessEvent) {
        val session: ClientSession = it.cast()
        val resolver = PaymentMethodDescriptorResolver(
          this@PrimerViewModel,
          model.configuredPaymentMethods,
          session.paymentMethods
        )

        paymentMethods.value = resolver.resolve()
        viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
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