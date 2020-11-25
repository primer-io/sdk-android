package io.primer.android.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.CheckoutConfig
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.api.APIClient
import io.primer.android.api.IAPIClient
import io.primer.android.logging.Logger
import io.primer.android.payment.MonetaryAmount
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory
import java.text.NumberFormat

class PrimerViewModel: ViewModel() {
  private val log = Logger("view-model")
  private var config: CheckoutConfig? = null
  private var session: ClientSession? = null

  final val uxMode: UniversalCheckout.UXMode
    get () = config!!.uxMode

  final val amount: MonetaryAmount?
    get () = config!!.amount

  val loading: MutableLiveData<Boolean> = MutableLiveData(true)

  fun initialize(config: CheckoutConfig) {
    this.config = config

    val token = ClientToken(config.clientToken)
    // TODO: remove the reliance on context here
    val api = APIClient(token)

    val sessionFactory = SessionFactory(api, token)

    sessionFactory.create {
      this.session = it
      this.loading.value = false
    }
  }
}