package io.primer.android.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.di.DIAppComponent
import io.primer.android.model.APIEndpoint
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.*
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
// FIXME inject dependencies via ctor
internal class TokenizationViewModel : ViewModel(), DIAppComponent {

    private var paymentMethod: PaymentMethodDescriptor? = null

    private val model: Model by inject()
    private val checkoutConfig: CheckoutConfig by inject()

    val submitted = MutableLiveData(false)
    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val tokenizationData = MutableLiveData<PaymentMethodTokenInternal>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(Collections.emptyList())

    fun resetPaymentMethod(pm: PaymentMethodDescriptor? = null) {
        paymentMethod = pm
        submitted.value = false
        tokenizationStatus.value = TokenizationStatus.NONE

        if (pm != null) {
            validationErrors.value = pm.validate()
        } else {
            validationErrors.value = Collections.emptyList()
        }
    }

    fun isValid(): Boolean = paymentMethod != null && (validationErrors.value?.isEmpty() == true)

    fun tokenize() {
        viewModelScope.launch {
            val method = paymentMethod ?: return@launch // FIXME this is failing silently
            when (val result = model.tokenize(method)) {
                is OperationResult.Success -> {
                    val paymentMethodToken: PaymentMethodTokenInternal = result.data
                    tokenizationData.postValue(paymentMethodToken)
                    tokenizationStatus.postValue(TokenizationStatus.SUCCESS)
                }
                is OperationResult.Error -> {
                    tokenizationError.postValue(Unit)
                    tokenizationStatus.postValue(TokenizationStatus.ERROR)
                }
            }
        }
    }

    // TODO: move this to vault view model ??
    fun deleteToken(token: PaymentMethodTokenInternal) {
        viewModelScope.launch {
            model.deleteToken(token)
        }
    }

    fun setTokenizableValue(key: String, value: String) {
        paymentMethod?.let { pm ->
            pm.setTokenizableValue(key, value)
            validationErrors.value = pm.validate()
        }
    }

    // TODO: move these payal things somewhere else
    val payPalBillingAgreementUrl = MutableLiveData<String>() // emits URI
    fun createPayPalBillingAgreement(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)

        viewModelScope.launch {
            when (val result = model.post(APIEndpoint.CREATE_PAYPAL_BILLING_AGREEMENT, body)) {
                is OperationResult.Success -> {
                    val approvalUrl = result.data.getString("approvalUrl")
                    payPalBillingAgreementUrl.postValue(approvalUrl)
                }
                is OperationResult.Error -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    val confirmPayPalBillingAgreement = MutableLiveData<JSONObject>()
    fun confirmPayPalBillingAgreement(id: String, token: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("tokenId", token)

        viewModelScope.launch {
            when (val result = model.post(APIEndpoint.CONFIRM_PAYPAL_BILLING_AGREEMENT, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    confirmPayPalBillingAgreement.postValue(data)
                }
                is OperationResult.Error -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    val payPalOrder = MutableLiveData<String>() // emits URI
    fun createPayPalOrder(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("amount", checkoutConfig.amount?.value)
        body.put("currencyCode", checkoutConfig.amount?.currency)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)

        viewModelScope.launch {
            when (val result = model.post(APIEndpoint.CREATE_PAYPAL_ORDER, body)) {
                is OperationResult.Success -> {
                    val uri = result.data.getString("approvalUrl")
                    payPalOrder.postValue(uri)
                }
                is OperationResult.Error -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    val goCardlessMandate = MutableLiveData<JSONObject>()
    val goCardlessMandateError = MutableLiveData<Unit>()
    fun createGoCardlessMandate(id: String, bankDetails: JSONObject, customerDetails: JSONObject) {
        val body = JSONObject()
        body.put("id", id)
        body.put("bankDetails", bankDetails)
        body.put("userDetails", customerDetails)

        Log.d("RUI", "TokenizationViewModel createGoCardlessMandate()")

        viewModelScope.launch { // this is running more than once after pressing "submit/confirm" @RUI

            Log.d("RUI", "viewModelScope.launch {}")

            when (val result = model.post(APIEndpoint.CREATE_GOCARDLESS_MANDATE, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    Log.d("RUI", "> posting data/result to 'goCardlessMandate'")
                    goCardlessMandate.postValue(data)
                }
                is OperationResult.Error -> {
                    Log.d("RUI", "> posting data/result to 'goCardlessMandateError'")
                    goCardlessMandateError.postValue(Unit)
                }
            }
        }
    }

    companion object {

        fun getInstance(owner: ViewModelStoreOwner): TokenizationViewModel {
            return ViewModelProvider(owner).get(TokenizationViewModel::class.java)
        }
    }
}
