package io.primer.android.model

import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckout
import io.primer.android.logging.Logger
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.dto.TokenType
import io.primer.android.payment.PaymentMethodDescriptor
import org.json.JSONObject

internal class Model(
  val config: CheckoutConfig,
  val configuredPaymentMethods: List<PaymentMethod>
) {
  private val log = Logger("model")
  private val clientToken = ClientToken.fromString(config.clientToken)
  private val api = APIClient(clientToken)
  private var clientSession: ClientSession? = null

  private val session: ClientSession
    get() = clientSession!!
  
  fun getConfiguration(): Observable {
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

  fun getVaultedPaymentMethods(): Observable {
    return api.get(
      APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)
    )
  }

  fun tokenize(tokenizable: PaymentMethodDescriptor): Observable {
    val json = JSONObject()

    json.put("paymentInstrument", tokenizable.toPaymentInstrument())

    if (config.uxMode == UniversalCheckout.UXMode.ADD_PAYMENT_METHOD) {
      json.put("tokenType", TokenType.MULTI_USE.name)
      json.put("paymentFlow", "VAULT")
    }

    return api.post(
      APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS),
      json
    )
  }
}