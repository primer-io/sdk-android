package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.di.DIAppComponent
import io.primer.android.logging.Logger
import io.primer.android.model.APIEndpoint
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
internal class TokenizationViewModel : BaseViewModel(), DIAppComponent {
  private val log = Logger("tokenization-view-model")
  private var paymentMethod: PaymentMethodDescriptor? = null
  private val model: Model by inject()
  private val checkoutConfig: CheckoutConfig by inject()

  val submitted = MutableLiveData(false)

  val status = MutableLiveData(TokenizationStatus.NONE)

  val error = MutableLiveData<APIError?>(null)

  val result = MutableLiveData<JSONObject>(null)

  val validationErrors: MutableLiveData<List<SyncValidationError>> =
    MutableLiveData(Collections.emptyList())

  fun reset(pm: PaymentMethodDescriptor? = null) {
    paymentMethod = pm
    submitted.value = false
    status.value = TokenizationStatus.NONE
    error.value = null
    result.value = null

    if (pm != null) {
      validationErrors.value = pm.validate()
    } else {
      validationErrors.value = Collections.emptyList()
    }
  }

  fun isValid(): Boolean {
    return paymentMethod != null && (validationErrors.value?.isEmpty() == true)
  }

  fun tokenize(): Observable {
    return model.tokenize(paymentMethod!!).observe {
      when (it) {
        is Observable.ObservableLoadingEvent -> {
          status.value = TokenizationStatus.LOADING
        }
        is Observable.ObservableSuccessEvent -> {
          result.value = it.data
          status.value = TokenizationStatus.SUCCESS
        }
        is Observable.ObservableErrorEvent -> {
          error.value = it.error
          status.value = TokenizationStatus.ERROR
        }
      }
    }
  }

  // TODO: move this to vault view model ??
  fun deleteToken(token: PaymentMethodToken) {
    model.deleteToken(token)
  }

  fun setTokenizableValue(key: String, value: String) {
    paymentMethod?.let { pm ->
      pm.setTokenizableValue(key, value)
      validationErrors.value = pm.validate()
    }
  }

  // TODO: move these payal things somewhere else
  fun createPayPalBillingAgreement(id: String, returnUrl: String, cancelUrl: String): Observable {
    val body = JSONObject()
    body.put("paymentMethodConfigId", id)
    body.put("returnUrl", returnUrl)
    body.put("cancelUrl", cancelUrl)
    return model.post(APIEndpoint.CREATE_PAYPAL_BILLING_AGREEMENT, body)
  }

  fun confirmPayPalBillingAgreement(id: String, token: String): Observable {
    val body = JSONObject()
    body.put("paymentMethodConfigId", id)
    body.put("tokenId", token)
    return model.post(APIEndpoint.CONFIRM_PAYPAL_BILLING_AGREEMENT, body)
  }

  fun createPayPalOrder(id: String, returnUrl: String, cancelUrl: String): Observable {
    val body = JSONObject()
    body.put("paymentMethodConfigId", id)
    body.put("amount", checkoutConfig.amount?.value)
    body.put("currencyCode", checkoutConfig.amount?.currency)
    body.put("returnUrl", returnUrl)
    body.put("cancelUrl", cancelUrl)

    return model.post(APIEndpoint.CREATE_PAYPAL_ORDER, body)
  }

  fun createGoCardlessMandate(
    id: String,
    bankDetails: JSONObject,
    customerDetails: JSONObject
  ): Observable {
    val body = JSONObject()

    body.put("id", id)
    body.put("bankDetails", bankDetails)
    body.put("userDetails", customerDetails)

    return model.post(APIEndpoint.CREATE_GOCARDLESS_MANDATE, body)
  }

  companion object {
    fun getInstance(owner: ViewModelStoreOwner): TokenizationViewModel {
      return ViewModelProvider(owner).get(TokenizationViewModel::class.java)
    }
  }
}