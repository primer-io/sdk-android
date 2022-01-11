package io.primer.android.viewmodel

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wallet.PaymentData
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.model.APIEndpoint
import io.primer.android.model.ApayaPaymentData
import io.primer.android.model.KlarnaPaymentData
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import java.util.Collections

@KoinApiExtension
internal class TokenizationViewModel(
    private val model: Model,
    private val config: PrimerConfig,
    private val tokenizationInteractor: TokenizationInteractor,
    private val apayaSessionInteractor: ApayaSessionInteractor,
) : ViewModel(), DIAppComponent {

    private var paymentMethod: PaymentMethodDescriptor? = null

    val submitted = MutableLiveData(false)
    val error = MutableLiveData<String?>()
    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val tokenizationData = MutableLiveData<PaymentMethodTokenInternal>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(
        Collections.emptyList()
    )
    private val _tokenizationCanceled = MutableLiveData<Unit>()
    val tokenizationCanceled: LiveData<Unit> = _tokenizationCanceled

    val klarnaError = MutableLiveData<Unit>()
    val klarnaPaymentData = MutableLiveData<KlarnaPaymentData>()
    val vaultedKlarnaPayment = MutableLiveData<JSONObject>()

    val payPalBillingAgreementUrl = MutableLiveData<String>() // emits URI
    val confirmPayPalBillingAgreement = MutableLiveData<JSONObject>()
    val payPalOrder = MutableLiveData<String>() // emits URI

    val goCardlessMandate = MutableLiveData<JSONObject>()
    val goCardlessMandateError = MutableLiveData<Unit>()

    val apayaPaymentData = MutableLiveData<ApayaPaymentData>()
    val apayaValidationData = MutableLiveData<Unit>()

    fun resetPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor? = null) {
        paymentMethod = paymentMethodDescriptor
        submitted.postValue(false)
        tokenizationStatus.postValue(TokenizationStatus.NONE)

        if (paymentMethodDescriptor != null) {
            validationErrors.postValue(paymentMethodDescriptor.validate())
        } else {
            validationErrors.postValue(Collections.emptyList())
        }
    }

    fun isValid(): Boolean =
        paymentMethod != null && (validationErrors.value?.isEmpty() == true)

    fun tokenize() {
        viewModelScope.launch {
            tokenizationInteractor(
                TokenizationParams(
                    paymentMethod ?: return@launch,
                    config.paymentMethodIntent,
                    config.settings.options.is3DSOnVaultingEnabled
                )
            )
                .onStart { tokenizationStatus.postValue(TokenizationStatus.LOADING) }
                .catch {
                    tokenizationStatus.postValue(TokenizationStatus.ERROR)
                }
                .collect {
                    tokenizationStatus.postValue(TokenizationStatus.SUCCESS)
                }
        }
    }

    fun setTokenizableValue(key: String, value: String) {
        paymentMethod?.let { pm ->
            pm.setTokenizableValue(key, value)
            validationErrors.value = pm.validate()
        }
    }

    fun setCardHasZipCode(value: Boolean) = (paymentMethod as? CreditCard)
        ?.let { card -> card.hasPostalCode = value }

    fun setCardHasCardholderName(value: Boolean) = (paymentMethod as? CreditCard)
        ?.let { card -> card.hasCardholderName = value }

    fun userCanceled() {
        _tokenizationCanceled.postValue(Unit)
    }

    fun handleWebFlowRequestResult(
        paymentMethodDescriptor: PaymentMethodDescriptor?,
        redirectUrl: String?,
    ) {
        when (paymentMethodDescriptor) {
            is KlarnaDescriptor -> handleKlarnaRequestResult(paymentMethodDescriptor, redirectUrl)
            is ApayaDescriptor -> handleApayaRequestResult(paymentMethodDescriptor, redirectUrl)
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

    private fun generateLocaleJson(): JSONObject = JSONObject().apply {
        val countryCode = config.settings.order.countryCode
        val locale = config.settings.options.locale.toLanguageTag()
        val currencyCode = config.settings.currency

        put("countryCode", countryCode)
        put("currencyCode", currencyCode)
        put("localeCode", locale)
    }

    fun createKlarnaPaymentSession(id: String, returnUrl: String, klarna: KlarnaDescriptor) {
        viewModelScope.launch {
            val localeData = generateLocaleJson()

            // FIXME a klarna flow that is not recurring requires every url to start with https://
            val body = JSONObject().apply {
                put("paymentMethodConfigId", id)
                put("sessionType", "RECURRING_PAYMENT")
                put("redirectUrl", returnUrl)
                put("localeData", localeData)

                klarna.options.orderDescription?.let { put("description", it) }
            }

            when (val result = model.post(APIEndpoint.CREATE_KLARNA_PAYMENT_SESSION, body)) {
                is OperationResult.Success -> {
                    val hppRedirectUrl = result.data.getString("hppRedirectUrl")
                    val sessionId = result.data.getString("sessionId")
                    klarnaPaymentData.postValue(
                        KlarnaPaymentData(
                            redirectUrl = hppRedirectUrl,
                            returnUrl = returnUrl,
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

    private fun handleKlarnaRequestResult(klarna: KlarnaDescriptor?, redirectUrl: String?) {
        // TODO move uri parsing to collaborator
        val uri = Uri.parse(redirectUrl)
        val klarnaAuthToken = uri.getQueryParameter("token")

        if (redirectUrl == null || klarna == null ||
            klarna.config.id == null || klarnaAuthToken == null
        ) {
            return klarnaError.postValue(Unit)
        }

        vaultKlarnaPayment(klarna.config.id, klarnaAuthToken, klarna)
    }

    fun handleRecurringKlarnaRequestResult(klarna: KlarnaDescriptor?, klarnaAuthToken: String) {

        if (klarna == null || klarna.config.id == null) {
            return klarnaError.postValue(Unit)
        }

        vaultKlarnaPayment(klarna.config.id, klarnaAuthToken, klarna)
    }

    fun vaultKlarnaPayment(id: String, token: String, klarna: KlarnaDescriptor) {
        val localeData = generateLocaleJson()

        val body = JSONObject()
        val sessionId = klarnaPaymentData.value?.sessionId

        body.put("paymentMethodConfigId", id)
        body.put("sessionId", sessionId)
        body.put("authorizationToken", token)
        body.put("localeData", localeData)

        klarna.options.orderDescription?.let { body.put("description", it) }

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
                    val description = "Failed to load PayPal."
                    error.postValue(description)
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
                    val description = "Failed to connect PayPal account."
                    error.postValue(description)
                }
            }
        }
    }

    fun createPayPalOrder(id: String, returnUrl: String, cancelUrl: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("amount", config.settings.currentAmount)
        body.put("currencyCode", config.settings.currency)
        body.put("returnUrl", returnUrl)
        body.put("cancelUrl", cancelUrl)

        viewModelScope.launch {
            when (val result = model.post(APIEndpoint.CREATE_PAYPAL_ORDER, body)) {
                is OperationResult.Success -> {
                    val uri = result.data.getString("approvalUrl")
                    payPalOrder.postValue(uri)
                }
                is OperationResult.Error -> {
                    val description = "Failed to load PayPal."
                    error.postValue(description)
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

    // region Apaya
    fun getApayaToken(merchantAccountId: String) {
        viewModelScope.launch {
            apayaSessionInteractor(
                ApayaSessionParams(
                    merchantAccountId,
                    config.settings.options.locale,
                    config.settings.currency.orEmpty(),
                    config.settings.customer.mobilePhone.orEmpty()
                )
            ).collect { apayaPaymentData.postValue(it) }
        }
    }

    private fun handleApayaRequestResult(apaya: ApayaDescriptor?, redirectUrl: String?) {
        viewModelScope.launch {
            val params = ApayaWebResultParams(Uri.parse(redirectUrl))
            apayaSessionInteractor.validateWebResultParams(
                ApayaWebResultParams(
                    Uri.parse(
                        redirectUrl
                    )
                )
            ).onEach {
                // TODO this needs a different approach using polymorphism for all payment descriptors!
                apaya?.apply {
                    setTokenizableValue("mx", params.mxNumber)
                    setTokenizableValue("mnc", params.mnc)
                    setTokenizableValue("mcc", params.mcc)
                    setTokenizableValue("hashedIdentifier", params.hashedIdentifier)
                    setTokenizableValue("productId", apaya.config.options?.merchantId.orEmpty())
                    setTokenizableValue(
                        "currencyCode",
                        localConfig.settings.currency.orEmpty()
                    )
                }
            }
                .collect {
                    apayaValidationData.postValue(it)
                }
        }
    }
    // endregion
}
