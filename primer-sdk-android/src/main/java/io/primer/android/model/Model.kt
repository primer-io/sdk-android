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
  private val clientToken = ClientToken(config.clientToken)
  private val api = APIClient(clientToken)
  private var clientSession: ClientSession? = null

  private val session: ClientSession
    get() = clientSession!!
  
  fun initialize(): Observable {
    return api.get(clientToken.configurationURL).observe {
      when (it) {
        is Observable.SuccessResult -> {
          log("Success! - ${it.data}")
          clientSession = ClientSession.fromJSON(it.data)
        }
        is Observable.ErrorResult -> {
          log("Noooo! - ${it.error.description}")
        }
        is Observable.PendingResult -> {
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