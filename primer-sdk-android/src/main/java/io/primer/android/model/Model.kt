package io.primer.android.model

import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckout
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.dto.*
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

    val url = APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)

    return api.post(url, json).observe {
      when (it) {
        is Observable.ObservableSuccessEvent -> {
          handleTokenizationResult(it)
        }
        is Observable.ObservableErrorEvent -> {
          handleTokenizationResult(it)
        }
      }
    }
  }

  fun deleteToken(token: PaymentMethodToken): Observable {
    val url = APIEndpoint.get(
      session,
      APIEndpoint.Target.PCI,
      APIEndpoint.DELETE_TOKEN,
      params = mapOf("id" to token.token)
    )

    return api.delete(url).observe {
      if (it is Observable.ObservableSuccessEvent) {
        EventBus.broadcast(CheckoutEvent.TokenRemovedFromVault(token))
      }
    }
  }

  fun post(pathname: String, body: JSONObject? = null): Observable {
    return api.post(APIEndpoint.get(session, APIEndpoint.Target.CORE, pathname), body)
  }

  private fun handleTokenizationResult(e: Observable.ObservableSuccessEvent) {
    val token: PaymentMethodToken = e.cast()

    EventBus.broadcast(CheckoutEvent.TokenizationSuccess(token))

    if (token.tokenType == TokenType.MULTI_USE) {
      EventBus.broadcast(CheckoutEvent.TokenAddedToVault(token))
    }
  }

  private fun handleTokenizationResult(e: Observable.ObservableErrorEvent) {
    EventBus.broadcast(CheckoutEvent.TokenizationError(e.error))
  }
}