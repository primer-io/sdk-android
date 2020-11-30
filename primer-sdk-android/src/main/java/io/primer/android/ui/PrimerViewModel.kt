package io.primer.android.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.UniversalCheckout
import io.primer.android.api.Observable
import io.primer.android.logging.Logger
import io.primer.android.model.Model
import io.primer.android.payment.MonetaryAmount
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodRemoteConfig
import java.util.*

internal class PrimerViewModel(
  private val model: Model
): ViewModel() {
  private val log = Logger("view-model")

  val loading: MutableLiveData<Boolean> = MutableLiveData(true)

  val paymentMethods: MutableLiveData<List<PaymentMethodRemoteConfig>> =
    MutableLiveData(Collections.emptyList())

  val uxMode: MutableLiveData<UniversalCheckout.UXMode> = MutableLiveData(model.config.uxMode)

  val amount: MutableLiveData<MonetaryAmount?> = MutableLiveData(model.config.amount)

  fun tokenize(paymentMethod: PaymentMethodDescriptor) {
    model.tokenize(paymentMethod)
  }

  fun initialize() {
    model.initialize().observe {
      if (it is Observable.SuccessResult) {

      }
    }
  }
}