package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import org.json.JSONObject
import java.util.*

internal class TokenizationViewModel(model: Model) : BaseViewModel(model) {
  private var paymentMethod: PaymentMethodDescriptor? = null

  val submitted = MutableLiveData<Boolean>(false)

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

  fun tokenize() {
    submitted.value = true

    if (validationErrors.value?.isNotEmpty() == true) {
      return
    }

    paymentMethod?.let { pm ->
      model.tokenize(pm).observe {
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
  }

  // TODO: move this to vault view model ??
  fun deleteToken(token: PaymentMethodToken) {
    model.deleteToken(token)
  }

  fun setTokenizableValue(key: String, value: String) {
    paymentMethod.let { pm ->
      if (pm != null) {
        pm.setTokenizableValue(key, value)
        validationErrors.value = pm.validate()
      }
    }
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