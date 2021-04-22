package io.primer.android.viewmodel

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wallet.PaymentData
import io.primer.android.di.DIAppComponent
import io.primer.android.model.APIEndpoint
import io.primer.android.model.KlarnaPaymentData
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.Collections

@KoinApiExtension // FIXME inject dependencies via ctor
internal class TokenizationViewModel constructor(
//    private val model: Model,
//    private val checkoutConfig: CheckoutConfig
) : ViewModel(), DIAppComponent {

    companion object {

        fun getInstance(owner: ViewModelStoreOwner): TokenizationViewModel {
            return ViewModelProvider(owner).get(TokenizationViewModel::class.java)
        }
    }

    private var paymentMethod: PaymentMethodDescriptor? = null

    private val model: Model by inject()
    private val checkoutConfig: CheckoutConfig by inject()

    val submitted = MutableLiveData(false)
    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val tokenizationData = MutableLiveData<PaymentMethodTokenInternal>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(
        Collections.emptyList()
    )

    val klarnaError = MutableLiveData<Unit>()
    val klarnaPaymentData = MutableLiveData<KlarnaPaymentData>()
    val vaultedKlarnaPayment = MutableLiveData<JSONObject>()

    val payPalBillingAgreementUrl = MutableLiveData<String>() // emits URI
    val confirmPayPalBillingAgreement = MutableLiveData<JSONObject>()
    val payPalOrder = MutableLiveData<String>() // emits URI

    val goCardlessMandate = MutableLiveData<JSONObject>()
    val goCardlessMandateError = MutableLiveData<Unit>()

    fun resetPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor? = null) {
        paymentMethod = paymentMethodDescriptor
        submitted.value = false
        tokenizationStatus.value = TokenizationStatus.NONE

        if (paymentMethodDescriptor != null) {
            validationErrors.value = paymentMethodDescriptor.validate()
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

    // region GOOGLE PAY
    fun handleGooglePayRequestResult(paymentData: PaymentData?, googlePay: GooglePayDescriptor?) {
        val paymentInformation = paymentData?.toJson() ?: return
        val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
        val token = paymentMethodData
            .getJSONObject("tokenizationData")
            .getString("token")

        val merchantId = googlePay?.merchantId ?: return

        val base64Token = Base64.encodeToString(token.toByteArray(), Base64.NO_WRAP)

        googlePay.setTokenizableValue("merchantId", merchantId)
        googlePay.setTokenizableValue("encryptedPayload", base64Token)
        googlePay.setTokenizableValue("flow", "GATEWAY")

        tokenize()
    }
    // endregion

    // region KLARNA
    fun createKlarnaPaymentSession(id: String, returnUrl: String, klarna: KlarnaDescriptor) {
        viewModelScope.launch {
            val localeData = JSONObject().apply {
                val countryCode = checkoutConfig.locale.country
                val currencyCode = checkoutConfig.monetaryAmount?.currency
                val locale = checkoutConfig.locale.toLanguageTag()

                put("countryCode", countryCode)
                put("currencyCode", currencyCode)
                put("localeCode", locale)
            }

            val orderItems = JSONArray().apply {
                klarna.options.orderItems.forEach {
                    val item = JSONObject().apply {
                        put("name", it.name)
                        put("unitAmount", it.unitAmount)
                        put("quantity", it.quantity)
                    }
                    put(item)
                }
            }

            val klarnaReturnUrl = "https://$returnUrl"

            val body = JSONObject().apply {
                put("paymentMethodConfigId", id)
                put("sessionType", "HOSTED_PAYMENT_PAGE")
                put("redirectUrl", klarnaReturnUrl)
                put("totalAmount", checkoutConfig.monetaryAmount?.value)
                put("localeData", localeData)
                put("orderItems", orderItems)
            }

            when (val result = model.post(APIEndpoint.CREATE_KLARNA_PAYMENT_SESSION, body)) {
                is OperationResult.Success -> {
                    val hppRedirectUrl = result.data.getString("hppRedirectUrl")
                    val sessionId = result.data.getString("sessionId")
                    klarnaPaymentData.postValue(
                        KlarnaPaymentData(
                            redirectUrl = hppRedirectUrl,
                            returnUrl = klarnaReturnUrl,
                            sessionId = sessionId
                        )
                    )
                }
                is OperationResult.Error -> {
                    klarnaError.postValue(Unit)
                }
            }
        }
    }

    fun handleKlarnaRequestResult(redirectUrl: String?, klarna: KlarnaDescriptor?) {
        // TODO move uri parsing to collaborator
        val uri = Uri.parse(redirectUrl)
        val klarnaAuthToken = uri.getQueryParameter("token")

        if (redirectUrl == null || klarna == null ||
            klarna.config.id == null || klarnaAuthToken == null
        ) {
            // TODO error: missing fields
            return
        }

        vaultKlarnaPayment(klarna.config.id, klarnaAuthToken, klarna)
    }

    fun vaultKlarnaPayment(id: String, token: String, klarna: KlarnaDescriptor) {
        val localeData = JSONObject().apply {
            val countryCode = checkoutConfig.locale.country
            val currencyCode = checkoutConfig.monetaryAmount?.currency
            val locale = checkoutConfig.locale.toLanguageTag()

            put("countryCode", countryCode)
            put("currencyCode", currencyCode)
            put("localeCode", locale)
        }

        val description = klarna.options.orderItems
            .map { it.name }
            .reduce { acc, s -> "$acc;$s" }

        val body = JSONObject()
        val sessionId = klarnaPaymentData.value?.sessionId

        body.put("paymentMethodConfigId", id)
        body.put("sessionId", sessionId)
        body.put("authorizationToken", token)
        body.put("description", description)
        body.put("localeData", localeData)

        viewModelScope.launch {
            when (val result = model.post(APIEndpoint.VAULT_KLARNA_PAYMENT, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    data.put("klarnaAuthorizationToken", token)
                    vaultedKlarnaPayment.postValue(data)
                }
                is OperationResult.Error -> {
                    klarnaError.postValue(Unit)
                }
            }
        }
    }
    // endregion

    // region PAYPAL
    // TODO: move these payal things somewhere else
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

    fun createPayPalOrder(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("amount", checkoutConfig.monetaryAmount?.value)
        body.put("currencyCode", checkoutConfig.monetaryAmount?.currency)
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
    // endregion

    // region GO CARDLESS
    fun createGoCardlessMandate(id: String, bankDetails: JSONObject, customerDetails: JSONObject) {
        val body = JSONObject()
        body.put("id", id)
        body.put("bankDetails", bankDetails)
        body.put("userDetails", customerDetails)

        viewModelScope.launch {

            when (val result = model.post(APIEndpoint.CREATE_GOCARDLESS_MANDATE, body)) {
                is OperationResult.Success -> {
                    val data = result.data
                    goCardlessMandate.postValue(data)
                }
                is OperationResult.Error -> {
                    goCardlessMandateError.postValue(Unit)
                }
            }
        }
    }
    // endregion
}
