package io.primer.android.model

import io.primer.android.CheckoutConfig
import io.primer.android.PaymentMethod
import io.primer.android.api.APIClient
import io.primer.android.api.Observable
import io.primer.android.logging.Logger
import io.primer.android.payment.ITokenizable
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import org.json.JSONObject

class Model(
  val config: CheckoutConfig,
  val configuredPaymentMethods: List<PaymentMethod>
) {
  private val log = Logger("model")
  private val clientToken = ClientToken.fromString(config.clientToken)
  private val api = APIClient(clientToken)
  private var clientSession: ClientSession? = null

  private val session: ClientSession
    get() = clientSession!!
  
  fun initialize(): Observable {
    return api.get(clientToken.configurationUrl).observe {
      when (it) {
        is Observable.ObservableSuccessEvent -> {
          clientSession = it.cast()
          log("Success! - " + clientSession.toString())
        }
        is Observable.ObservableErrorEvent -> {
          log("Noooo! - ${it.error.description}")
        }
        is Observable.ObservableLoadingEvent -> {
          log("still loading...")
        }
      }
    }
  }

  fun tokenize(tokenizable: ITokenizable): Observable {
    val json = JSONObject()

    json.put("paymentInstrument", tokenizable.toPaymentInstrument())

    val url = "${session.pciUrl}/payment-instruments"

    return api.post(url, json)
  }
}