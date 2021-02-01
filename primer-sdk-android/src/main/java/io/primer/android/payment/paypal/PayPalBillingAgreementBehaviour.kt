package io.primer.android.payment.paypal

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.model.Observable
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.ViewStatus
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PayPalBillingAgreementBehaviour(
  private val paypal: PayPal,
  private val viewModel: PrimerViewModel,
) : WebBrowserIntentBehaviour() {
  private val log = Logger("paypal.billingagreement")

  override fun initialize() {
    tokenizationViewModel?.reset(paypal)
  }

  override fun getUri(cancelUrl: String, returnUrl: String, callback: ((String) -> Unit)) {
    paypal.config.id?.let {
      tokenizationViewModel?.createPayPalBillingAgreement(
        id = it,
        returnUrl = returnUrl,
        cancelUrl = cancelUrl
      )?.observe { e ->
        when (e) {
          is Observable.ObservableSuccessEvent -> {
            callback(e.data.getString("approvalUrl"))
          }
        }
      }
    }
  }

  override fun onSuccess(uri: Uri) {
    uri.getQueryParameter("ba_token")?.let { token ->
      paypal.config.id?.let {
        tokenizationViewModel?.confirmPayPalBillingAgreement(id = it, token = token)?.observe { e ->
          when (e) {
            is Observable.ObservableSuccessEvent -> tokenize(e.data)
          }
        }
      }
    }
  }

  private fun tokenize(data: JSONObject) {
    paypal.setTokenizableValue("paypalBillingAgreementId", data.getString("billingAgreementId"))
    paypal.setTokenizableValue("externalPayerInfo", data.getJSONObject("externalPayerInfo"))
    paypal.setTokenizableValue("shippingAddress", data.getJSONObject("shippingAddress"))

    tokenizationViewModel?.tokenize()?.observe {
      when (it) {
        is Observable.ObservableSuccessEvent -> {
          viewModel.viewStatus.value = ViewStatus.VIEW_VAULTED_PAYMENT_METHODS
        }
      }
    }
  }

  override fun onCancel(uri: Uri?) {
    log.warn("User cancelled paypal billing agreement")
  }
}