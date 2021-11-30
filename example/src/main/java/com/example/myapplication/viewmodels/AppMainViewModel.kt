package com.example.myapplication.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.repositories.ClientTokenRepository
import com.example.myapplication.repositories.PaymentsRepository
import com.example.myapplication.datamodels.*
import com.example.myapplication.repositories.ClientSessionRepository
import com.example.myapplication.repositories.ResumeRepository
import com.example.myapplication.utils.AmountUtils
import com.example.myapplication.utils.CombinedLiveData
import io.primer.android.CheckoutEventListener
import io.primer.android.PaymentMethod
import io.primer.android.Primer
import io.primer.android.completion.ResumeHandler
import io.primer.android.model.OrderItem
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.dto.*
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.card.Card
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.threeds.data.models.ResponseCode
import okhttp3.OkHttpClient
import java.util.UUID

@Keep
class AppMainViewModel : ViewModel() {

    private val clientTokenRepository = ClientTokenRepository()
    private val clientSessionRepository = ClientSessionRepository()

    private val paymentsRepository = PaymentsRepository()
    private val resumeRepository = ResumeRepository()

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    private val _transactionId: MutableLiveData<String?> = MutableLiveData<String?>()

    val orderId: MutableLiveData<String> = MutableLiveData<String>(UUID.randomUUID().toString())

    val vaultedPaymentTokens: MutableLiveData<List<PaymentMethodToken>> = MutableLiveData(listOf())

    private val _threeDsEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val threeDsEnabled: LiveData<Boolean> = _threeDsEnabled

    private val _threeDsResult: MutableLiveData<PaymentMethodToken.AuthenticationDetails?> =
        MutableLiveData<PaymentMethodToken.AuthenticationDetails?>()
    val threeDsResult: LiveData<PaymentMethodToken.AuthenticationDetails?> = _threeDsResult
    fun clearThreeDsResult(): Unit =
        _threeDsResult.postValue(null)

    private val _customerId: MutableLiveData<String> = MutableLiveData<String>("customer8")
    val customerId: LiveData<String> = _customerId
    fun setCustomerId(id: String): Unit =
        _customerId.postValue(id)

    private val _transactionResponse: MutableLiveData<TransactionResponse> = MutableLiveData()
    val transactionResponse: LiveData<TransactionResponse> = _transactionResponse

    val environment: MutableLiveData<PrimerEnv> = MutableLiveData<PrimerEnv>(PrimerEnv.Sandbox)

    private val _amount: MutableLiveData<Int> = MutableLiveData<Int>(50)
    val amount: LiveData<Int> = _amount
    val amountStringified: String get() = String.format("%.2f", _amount.value!!.toDouble() / 100)
    fun setAmount(amount: Int): Unit =
        _amount.postValue(amount)

    val countryCode: MutableLiveData<AppCountryCode> = MutableLiveData(AppCountryCode.GB)
    val clientToken: MutableLiveData<String?> = MutableLiveData<String?>()
    val useKlarna: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val usePayPal: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val useCard: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val useGooglePay: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val usePayMobile: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    fun resetTransactionState(): Unit =
        _transactionState.postValue(TransactionState.IDLE)

    private fun canLaunch(id: String?, amt: Int?) =
        !id.isNullOrEmpty() && (amt ?: 0) > 0

    val canLaunchPrimer: MutableLiveData<Boolean> =
        CombinedLiveData(customerId, _amount, ::canLaunch)

    val vaultDisabled: Boolean get() = isStandalonePaymentMethod && useGooglePay.value == true
    val isStandalonePaymentMethod: Boolean
        get() = listOf(
            useKlarna.value,
            usePayPal.value,
            useCard.value,
            useGooglePay.value,
            usePayMobile.value
        )
            .count { it == true } == 1

    val paymentMethodList: List<PaymentMethod>
        get() {
            val list = mutableListOf<PaymentMethod>()
            if (useCard.value == true) list.add(Card())
            if (useKlarna.value == true) list.add(Klarna())
            if (usePayPal.value == true) list.add(PayPal())
            if (usePayMobile.value == true) list.add(Apaya())
            if (useGooglePay.value == true) list.add(
                GooglePay(
                    merchantName = "Primer",
                    totalPrice = getAmountConverted().toString(),
                    countryCode = config.settings.order.countryCode.toString(),
                    currencyCode = config.settings.currency,
                )
            )
            return list
        }

    val config: PrimerConfig
        get() = PrimerConfig(
            // todo: refactor to reintroduce custom values through client session
            settings = PrimerSettings(
                options = Options(
                    preferWebView = true,
                    debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
                    is3DSOnVaultingEnabled = threeDsEnabled.value ?: false,
                    redirectScheme = "primer"
                )
            ),
        )

    fun configure(listener: CheckoutEventListener) {
        Primer.instance.configure(config, listener)
    }

    fun fetchClientToken() {
        clientTokenRepository.fetch(
            customerId.value ?: return,
            (environment.value ?: return).environment,
            countryCode.value.toString(),
            client
        ) { t -> clientToken.postValue(t) }
    }

    fun fetchClientSession() {
        clientSessionRepository.fetch(
            client,
            (environment.value ?: return).environment,
            countryCode.value?.currencyCode.toString(),
            amount.value!!,
            countryCode.value?.mapped!!
        ) { t -> clientToken.postValue(t) }
    }

    fun createTransaction(
        paymentMethod: PaymentMethodToken,
        completion: ResumeHandler? = null
    ) {
        _transactionId.postValue(null)

        val environment = environment.value!!.environment
        val body = TransactionRequest.create(paymentMethod.token, environment)

        paymentsRepository.create(
            body,
            client,
            { result ->
                _transactionResponse.postValue(result)
                if (result.requiredAction?.name != null) {
                    _transactionId.postValue(result.id)
                    completion?.handleNewClientToken(result.requiredAction.clientToken.orEmpty())
                }
            },
        ) { status ->
            val state = status.toTransactionState()
            when (state) {
                TransactionState.ERROR -> {
                    completion?.handleError(Error(state.name))
                }
                TransactionState.SUCCESS -> {
                    completion?.handleSuccess()
                }
            }
            _transactionState.postValue(state)
        }
    }

    fun resumePayment(token: String, completion: ResumeHandler? = null) {
        val body = ResumePaymentRequest(
            _transactionId.value.orEmpty(),
            token,
            environment.value?.environment.orEmpty()
        )
        resumeRepository.create(
            body,
            client,
            { result ->
                _transactionResponse.postValue(result)
            },
        ) { status ->
            val state = status.toTransactionState()
            when (state) {
                TransactionState.ERROR -> {
                    completion?.handleError(Error(state.name))
                }
                TransactionState.PENDING -> {
                    completion?.handleError(Error())
                }
                else -> {
                    completion?.handleSuccess()
                }
            }
            _transactionState.postValue(state)
        }
    }

    // 3DS
    internal fun handleTokenData(paymentMethodToken: PaymentMethodToken) {
        when {
            paymentMethodToken.tokenType == TokenType.SINGLE_USE
                && (paymentMethodToken.threeDSecureAuthentication?.responseCode == ResponseCode.AUTH_SUCCESS
                || paymentMethodToken.threeDSecureAuthentication == null) -> {
                createTransaction(paymentMethodToken)
            }
            paymentMethodToken.threeDSecureAuthentication != null -> _threeDsResult.postValue(
                paymentMethodToken.threeDSecureAuthentication
            )
            else -> return
        }
    }

    fun getPrimerPaymentMethod(paymentMethod: PaymentMethod): PrimerPaymentMethod {
        return when (paymentMethod) {
            is Klarna -> PrimerPaymentMethod.KLARNA
            is Card -> PrimerPaymentMethod.CARD
            is PayPal -> PrimerPaymentMethod.PAYPAL
            is GooglePay -> PrimerPaymentMethod.GOOGLE_PAY
            is GoCardless -> PrimerPaymentMethod.GOCARDLESS
            is Apaya -> PrimerPaymentMethod.APAYA
            else -> PrimerPaymentMethod.CARD
        }
    }

    fun getAmountConverted(): Int {
        return AmountUtils.covert(amount.value!!, countryCode.value?.currencyCode!!)
    }
}
