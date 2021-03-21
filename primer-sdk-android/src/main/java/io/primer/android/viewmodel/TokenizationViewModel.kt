package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.di.DIAppComponent
import io.primer.android.model.APIEndpoint
import io.primer.android.model.Model
import io.primer.android.model.Observable
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

@KoinApiExtension // FIXME inject dependencies via ctor
internal class TokenizationViewModel(
    // private val model: Model,
    // private val checkoutConfig: CheckoutConfig
) : ViewModel(), DIAppComponent {

    private var paymentMethod: PaymentMethodDescriptor? = null

    private val model: Model by inject()
    private val checkoutConfig: CheckoutConfig by inject()

    val submitted = MutableLiveData(false)
    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val tokenizationData = MutableLiveData<PaymentMethodTokenInternal>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(Collections.emptyList())

    fun reset(pm: PaymentMethodDescriptor? = null) {
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

    //
    val _payPalBillingAgreementUrl = MutableLiveData<String>()
    fun _createPayPalBillingAgreement(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)

        viewModelScope.launch {
            when (val result = model._post(APIEndpoint.CREATE_PAYPAL_BILLING_AGREEMENT, body)) {
                is OperationResult.Success -> {
                    val approvalUrl = result.data.getString("approvalUrl")
                    _payPalBillingAgreementUrl.postValue(approvalUrl)
                }
                is OperationResult.Error -> {
                    //
                }
            }
        }
    }
    fun createPayPalBillingAgreement(id: String, returnUrl: String, cancelUrl: String): Observable {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)
        return model.post(APIEndpoint.CREATE_PAYPAL_BILLING_AGREEMENT, body)
    }
    //

    //
    val _confirmPayPalBillingAgreement = MutableLiveData<JSONObject>()
    fun _confirmPayPalBillingAgreement(id: String, token: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("tokenId", token)

        viewModelScope.launch {
            when (val result = model._post(APIEndpoint.CONFIRM_PAYPAL_BILLING_AGREEMENT, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    _confirmPayPalBillingAgreement.postValue(data)
                }
                is OperationResult.Error -> {
                    //
                }
            }
        }
    }
    fun confirmPayPalBillingAgreement(id: String, token: String): Observable {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("tokenId", token)
        return model.post(APIEndpoint.CONFIRM_PAYPAL_BILLING_AGREEMENT, body)
    }
    //

    //
    val _payPalOrder = MutableLiveData<JSONObject>()
    fun _createPayPalOrder(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("amount", checkoutConfig.amount?.value)
        body.put("currencyCode", checkoutConfig.amount?.currency)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)

        viewModelScope.launch {
            when (val result = model._post(APIEndpoint.CREATE_PAYPAL_ORDER, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    _payPalOrder.postValue(data)
                }
                is OperationResult.Error -> {
                    //
                }
            }
        }
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
    //

    //
    val _goCardlessMandate = MutableLiveData<JSONObject>()
    val _goCardlessMandateError = MutableLiveData<Unit>()
    fun _createGoCardlessMandate(id: String, bankDetails: JSONObject, customerDetails: JSONObject, ) {
        val body = JSONObject()
        body.put("id", id)
        body.put("bankDetails", bankDetails)
        body.put("userDetails", customerDetails)

        viewModelScope.launch {
            when (val result = model._post(APIEndpoint.CREATE_GOCARDLESS_MANDATE, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    _goCardlessMandate.postValue(data)
                }
                is OperationResult.Error -> {
                    //
                }
            }
        }
    }
    fun createGoCardlessMandate(id: String, bankDetails: JSONObject, customerDetails: JSONObject, ): Observable {
        val body = JSONObject()
        body.put("id", id)
        body.put("bankDetails", bankDetails)
        body.put("userDetails", customerDetails)

        return model.post(APIEndpoint.CREATE_GOCARDLESS_MANDATE, body)
    }
    //

    companion object {

        fun getInstance(owner: ViewModelStoreOwner): TokenizationViewModel {
            return ViewModelProvider(owner).get(TokenizationViewModel::class.java)
        }
    }
}
