package io.primer.android.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.CheckoutConfig
import io.primer.android.UniversalCheckout
import io.primer.android.api.APIClient
import io.primer.android.api.IAPIClient
import io.primer.android.logging.Logger
import io.primer.android.payment.MonetaryAmount
import io.primer.android.payment.PaymentMethod
import io.primer.android.payment.PaymentMethodRemoteConfig
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory
import org.json.JSONObject
import java.util.*

class PrimerViewModel: ViewModel() {
  private val log = Logger("view-model")
  private var config: CheckoutConfig? = null
  private var session: ClientSession? = null
  private var api: IAPIClient? = null

  val coreUrl: String
    get () = session!!.coreUrl

  val pciUrl: String
    get () = session!!.pciUrl

  val uxMode: UniversalCheckout.UXMode
    get () = config!!.uxMode

  val amount: MonetaryAmount?
    get () = config!!.amount

  val loading: MutableLiveData<Boolean> = MutableLiveData(true)

  val paymentMethods: MutableLiveData<List<PaymentMethodRemoteConfig>> =
    MutableLiveData(Collections.emptyList())

  fun tokenize(method: PaymentMethod) {
    val json = JSONObject()

    json.put("paymentInstrument", method.toPaymentInstrument())

    val url = "$pciUrl/payment-instruments"

    log("Starting tokenization of: ${method.id}")

    this.api?.post(
      url,
      json,
      {
        // TODO: handle tokenization success
        log("Tokenization success")
        log(it.data.toString())
      },
      {
        // TODO: Handle tokenization error
        log.error("Tokenization ERROR")
        log.error(it.data.description)
      }
    )
  }

  fun initialize(config: CheckoutConfig) {
    this.config = config

    val token = ClientToken(config.clientToken)
    // TODO: remove the reliance on context here
    api = APIClient(token)

    val sessionFactory = SessionFactory(api!!, token)

    sessionFactory.create {
      session = it
      this.paymentMethods.value = it.paymentMethods
      this.loading.value = false
    }
  }
}