package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.UniversalCheckout
import io.primer.android.api.Observable
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.payment.MonetaryAmount
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodRemoteConfig
import io.primer.android.session.ClientSession
import java.util.*

internal class PrimerViewModel(
  private val model: Model
): ViewModel() {
  private val log = Logger("view-model")

  val loading: MutableLiveData<Boolean> = MutableLiveData(true)

  // Select Payment Method
  val paymentMethods: MutableLiveData<List<PaymentMethodDescriptor>> =
    MutableLiveData(Collections.emptyList())

  val selectedPaymentMethod: MutableLiveData<PaymentMethodDescriptor?> = MutableLiveData(null)

  val uxMode: MutableLiveData<UniversalCheckout.UXMode> = MutableLiveData(model.config.uxMode)

  val amount: MutableLiveData<MonetaryAmount?> = MutableLiveData(model.config.amount)

  fun tokenize(paymentMethod: PaymentMethodDescriptor) {
    model.tokenize(paymentMethod)
  }

  fun initialize() {
    model.getConfiguration().observe {
      if (it is Observable.ObservableSuccessEvent) {
        val session: ClientSession = it.cast()
        val resolver = PaymentMethodDescriptorResolver(this, model.configuredPaymentMethods, session.paymentMethods)

        paymentMethods.value = resolver.resolve()
        loading.value = false
      }
    }
  }
}