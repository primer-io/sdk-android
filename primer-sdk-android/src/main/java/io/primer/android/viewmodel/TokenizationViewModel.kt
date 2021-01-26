package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.logging.Logger
import io.primer.android.model.APIEndpoint
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import org.json.JSONObject
import java.util.*

internal class TokenizationViewModel(model: Model) : BaseViewModel(model) {
  private val log = Logger("tokenization-view-model")
  private var paymentMethod: PaymentMethodDescriptor? = null

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
    body.put("amount", model.config.amount?.value)
    body.put("currencyCode", model.config.amount?.currency)
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


  class ProviderFactory(private val model: Model) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return TokenizationViewModel(model) as T
    }
  }

  companion object {
    fun getInstance(owner: ViewModelStoreOwner): TokenizationViewModel {
      return ViewModelProvider(owner).get(TokenizationViewModel::class.java)
    }
  }
}