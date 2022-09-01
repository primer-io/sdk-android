package io.primer.android.viewmodel

import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wallet.PaymentData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.apaya.models.ApayaPaymentData
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.paypal.PaypalOrderInfoInteractor
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.model.APIEndpoint
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.payment.paypal.PayPalDescriptor
import io.primer.android.ui.payment.klarna.KlarnaPaymentData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Collections

internal class TokenizationViewModel(
    private val model: Model,
    private val config: PrimerConfig,
    private val tokenizationInteractor: TokenizationInteractor,
    private val apayaSessionInteractor: ApayaSessionInteractor,
    private val paypalOrderInfoInteractor: PaypalOrderInfoInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor,
    private val klarnaDeeplinkInteractor: KlarnaDeeplinkInteractor
) : ViewModel(), DIAppComponent {

    private var paymentMethod: PaymentMethodDescriptor? = null

    val submitted = MutableLiveData(false)
    val error = MutableLiveData<Throwable>()
    val tokenizationResult = MutableLiveData<String>()

    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val tokenizationError = MutableLiveData<Unit>()
    val tokenizationData = MutableLiveData<PaymentMethodTokenInternal>()
    val validationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData(
        Collections.emptyList()
    )
    val autoFocusFields: MutableLiveData<Set<String>> = MutableLiveData(
        Collections.emptySet()
    )
    private val _tokenizationCanceled = MutableLiveData<String>()
    val tokenizationCanceled: LiveData<String> = _tokenizationCanceled

    val klarnaError = MutableLiveData<Throwable>()
    val klarnaPaymentData = MutableLiveData<KlarnaPaymentData>()
    val vaultedKlarnaPayment = MutableLiveData<JSONObject>()

    val payPalBillingAgreementUrl = MutableLiveData<String>() // emits URI
    val confirmPayPalBillingAgreement = MutableLiveData<JSONObject>()
    val payPalOrder = MutableLiveData<String>() // emits URI

    val goCardlessMandate = MutableLiveData<JSONObject>()
    val goCardlessMandateError = MutableLiveData<Throwable>()

    val apayaPaymentData = MutableLiveData<ApayaPaymentData>()
    val apayaValidationData = MutableLiveData<Unit>()

    private val _asyncRedirectUrl = MutableLiveData<String>()
    val asyncRedirectUrl: LiveData<String> = _asyncRedirectUrl

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
                    config.settings.paymentMethodOptions.cardPaymentOptions.is3DSOnVaultingEnabled
                )
            )
                .onStart { tokenizationStatus.postValue(TokenizationStatus.LOADING) }
                .catch {
                    tokenizationStatus.postValue(TokenizationStatus.ERROR)
                }
                .collect {
                    tokenizationResult.postValue(it)
                    tokenizationStatus.postValue(TokenizationStatus.SUCCESS)
                }
        }
    }

    fun setTokenizableValue(key: String, value: String, withValidation: Boolean = true) {
        paymentMethod?.let { pm ->
            pm.setTokenizableValue(key, value)
            if (withValidation) validationErrors.value = pm.validate()
            autoFocusFields.value = pm.getValidAutoFocusableFields()
        }
    }

    fun setCardHasFields(fields: Map<String, Boolean>?) {
        val availableFields = mutableMapOf<PrimerInputElementType, Boolean>()
        for ((key, value) in fields.orEmpty()) {
            PrimerInputElementType.fieldOf(key)?.let { fieldType ->
                availableFields[fieldType] = value
            }
        }
        (paymentMethod as? CreditCard)?.availableFields?.putAll(availableFields)
    }

    fun getDeeplinkUrl() = viewModelScope.launch {
        _asyncRedirectUrl.postValue(asyncPaymentMethodDeeplinkInteractor(None()))
    }

    fun userCanceled(paymentMethodType: String) {
        _tokenizationCanceled.postValue(paymentMethodType)
    }

    fun handleWebFlowRequestResult(
        paymentMethodDescriptor: PaymentMethodDescriptor?,
        redirectUrl: String?,
    ) {
        when (paymentMethodDescriptor) {
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
        val locale = config.settings.locale.toLanguageTag()
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

            when (
                val result = model.post(
                    APIEndpoint.CREATE_KLARNA_PAYMENT_SESSION,
                    PaymentMethodType.KLARNA,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val sessionId = result.data.getString("sessionId")
                    klarnaPaymentData.postValue(
                        KlarnaPaymentData(
                            redirectUrl = klarnaDeeplinkInteractor(None()),
                            returnUrl = returnUrl,
                            sessionId = sessionId,
                            clientToken = result.data.getString("clientToken")
                        )
                    )
                }
                is OperationResult.Error -> {
                    klarnaError.postValue(result.error)
                }
            }
        }
    }

    fun handleKlarnaRequestResult(klarna: KlarnaDescriptor?, authToken: String?) {
        if (klarna == null ||
            klarna.config.id == null || authToken == null
        ) {
            return klarnaError.postValue(Error())
        }

        vaultKlarnaPayment(klarna.config.id, authToken, klarna)
    }

    private fun vaultKlarnaPayment(id: String, token: String, klarna: KlarnaDescriptor) {
        val localeData = generateLocaleJson()

        val body = JSONObject()
        val sessionId = klarnaPaymentData.value?.sessionId

        body.put("paymentMethodConfigId", id)
        body.put("sessionId", sessionId)
        body.put("authorizationToken", token)
        body.put("localeData", localeData)

        klarna.options.orderDescription?.let { body.put("description", it) }

        viewModelScope.launch {
            when (
                val result = model.post(
                    APIEndpoint.VAULT_KLARNA_PAYMENT, PaymentMethodType.KLARNA,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val data = result.data
                    data.put("klarnaAuthorizationToken", token)
                    vaultedKlarnaPayment.postValue(data)
                }
                is OperationResult.Error -> {
                    klarnaError.postValue(result.error)
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
            when (
                val result = model.post(
                    APIEndpoint.CREATE_PAYPAL_BILLING_AGREEMENT,
                    PaymentMethodType.PAYPAL,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val approvalUrl = result.data.getString("approvalUrl")
                    payPalBillingAgreementUrl.postValue(approvalUrl)
                }
                is OperationResult.Error -> {
                    error.postValue(result.error)
                }
            }
        }
    }

    fun confirmPayPalBillingAgreement(id: String, token: String) {
        val body = JSONObject()
        body.put("paymentMethodConfigId", id)
        body.put("tokenId", token)

        viewModelScope.launch {
            when (
                val result = model.post(
                    APIEndpoint.CONFIRM_PAYPAL_BILLING_AGREEMENT,
                    PaymentMethodType.PAYPAL,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val data = result.data
                    confirmPayPalBillingAgreement.postValue(data)
                }
                is OperationResult.Error -> {
                    error.postValue(result.error)
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
            when (
                val result = model.post(
                    APIEndpoint.CREATE_PAYPAL_ORDER,
                    PaymentMethodType.PAYPAL,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val uri = result.data.getString("approvalUrl")
                    payPalOrder.postValue(uri)
                }
                is OperationResult.Error -> {
                    error.postValue(result.error)
                }
            }
        }
    }

    fun getPaypalOrderInfo(payPal: PayPalDescriptor, orderId: String) {
        viewModelScope.launch {
            paypalOrderInfoInteractor(PaypalOrderInfoParams(payPal.config.id.orEmpty(), orderId))
                .catch {
                    tokenize()
                }
                .collect {
                    payPal.setTokenizableValue(
                        "externalPayerInfo",
                        JSONObject().apply { put("email", it.email) }
                    )
                    tokenize()
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
            when (
                val result = model.post(
                    APIEndpoint.CREATE_GOCARDLESS_MANDATE,
                    PaymentMethodType.GOCARDLESS,
                    body
                )
            ) {
                is OperationResult.Success -> {
                    val data = result.data
                    goCardlessMandate.postValue(data)
                }
                is OperationResult.Error -> {
                    goCardlessMandateError.postValue(result.error)
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
                    config.settings.locale,
                    config.settings.currency,
                    config.settings.customer.mobileNumber.orEmpty()
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

    fun clearInputField(type: PrimerInputElementType) {
        paymentMethod?.clearInputField(type)
    }

    fun hasField(inputType: PrimerInputElementType): Boolean = paymentMethod
        ?.hasFieldValue(inputType) ?: false

    // endregion
}
