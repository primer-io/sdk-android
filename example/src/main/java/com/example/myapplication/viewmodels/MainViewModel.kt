package com.example.myapplication.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.repositories.PaymentsRepository
import com.example.myapplication.datamodels.*
import com.example.myapplication.repositories.ClientSessionRepository
import com.example.myapplication.repositories.CountryRepository
import com.example.myapplication.repositories.PaymentInstrumentsRepository
import com.example.myapplication.repositories.ResumeRepository
import com.example.myapplication.utils.AmountUtils
import com.example.myapplication.utils.CombinedLiveData
import io.primer.android.CheckoutEventListener
import io.primer.android.Primer
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.domain.action.models.Address
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.dto.*
import io.primer.android.threeds.data.models.ResponseCode
import okhttp3.OkHttpClient
import java.util.UUID

@Keep
class MainViewModel(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    private val clientSessionRepository = ClientSessionRepository()
    private val paymentInstrumentsRepository = PaymentInstrumentsRepository()
    private val paymentsRepository = PaymentsRepository()
    private val resumeRepository = ResumeRepository()

    enum class Mode {
        CHECKOUT, VAULT;

        val isVaulting: Boolean get() = this == VAULT
    }

    val mode = MutableLiveData(Mode.CHECKOUT)

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    private val _transactionId: MutableLiveData<String?> = MutableLiveData<String?>()

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

    private val _postalCode = MutableLiveData("")
    val postalCode: LiveData<String> = _postalCode

    private val _transactionResponse: MutableLiveData<TransactionResponse> = MutableLiveData()
    val transactionResponse: LiveData<TransactionResponse> = _transactionResponse

    val environment: MutableLiveData<PrimerEnv> = MutableLiveData<PrimerEnv>(PrimerEnv.Sandbox)

    private val _amount: MutableLiveData<Int> = MutableLiveData<Int>(2000)
    val amount: LiveData<Int> = _amount
    val amountStringified: String get() = String.format("%.2f", _amount.value!!.toDouble() / 100)
    fun setAmount(amount: Int): Unit = _amount.postValue(amount)

    private val _descriptor = MutableLiveData("Purchase: Item-123")
    val descriptor: LiveData<String> = _descriptor
    fun setDescriptor(descriptor: String) = _descriptor.postValue(descriptor)

    val countryCode: MutableLiveData<AppCountryCode> = MutableLiveData(AppCountryCode.DE)
    val clientToken: MutableLiveData<String?> = MutableLiveData<String?>()
    val useStandalonePaymentMethod: MutableLiveData<PrimerPaymentMethod> =
        MutableLiveData<PrimerPaymentMethod>()

    val paymentInstruments: MutableLiveData<List<PaymentMethodToken>> =
        MutableLiveData<List<PaymentMethodToken>>()

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    private val _paymentHandling: MutableLiveData<PaymentHandling> = MutableLiveData()
    fun setPaymentHandling(paymentHandling: PaymentHandling): Unit = _paymentHandling.postValue(
        paymentHandling
    )

    fun resetTransactionState(): Unit =
        _transactionState.postValue(TransactionState.IDLE)

    private fun canLaunch(id: String?, amt: Int?) =
        !id.isNullOrEmpty() && (amt ?: 0) > 0

    val canLaunchPrimer: MutableLiveData<Boolean> =
        CombinedLiveData(customerId, _amount, ::canLaunch)

    val vaultDisabled: Boolean
        get() = isStandalonePaymentMethod
            && useStandalonePaymentMethod.value == PrimerPaymentMethod.GOOGLE_PAY
    val isStandalonePaymentMethod: Boolean
        get() = useStandalonePaymentMethod.value != null

    val config: PrimerConfig
        get() = PrimerConfig(
            // todo: refactor to reintroduce custom values through client session
            settings = PrimerSettings(
                business = Business(
                    "Primer",
                    address = Address(
                        addressLine1 = "line1",
                        addressLine2 = "line2",
                        postalCode = "3455",
                        city = "London",
                        countryCode = CountryCode.GB
                    )
                ),
                options = Options(
                    preferWebView = true,
                    debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
                    is3DSOnVaultingEnabled = threeDsEnabled.value ?: false,
                    redirectScheme = "primer",
                    paymentHandling = _paymentHandling.value ?: PaymentHandling.AUTO
                )
            ),
        )

    fun configure(
        listener: CheckoutEventListener,
    ) {
        Primer.instance.configure(config, listener)
    }

    fun fetchClientSession() = clientSessionRepository.fetch(
        client,
        customerId.value.orEmpty(),
        UUID.randomUUID().toString(),
        amount.value!!,
        countryRepository.getCountry().name,
        countryRepository.getCurrency(),
        environment.value?.environment ?: throw Error("no environment set!")
    ) { t -> clientToken.postValue(t) }

    fun fetchPaymentInstruments() {
        paymentInstruments.postValue(listOf())
        paymentInstrumentsRepository.fetch(
            customerId.value.orEmpty(),
            environment.value?.environment ?: throw Error("no environment set!"),
            client
        ) { t -> paymentInstruments.postValue(t) }
    }

    fun createPayment(
        paymentMethod: PaymentMethodToken,
        completion: ResumeDecisionHandler? = null
    ) {
        _transactionId.postValue(null)

        val environment = environment.value!!.environment
        val body = TransactionRequest.create(
            paymentMethod.token,
            descriptor.value.orEmpty()
        )

        paymentsRepository.create(
            body,
            environment,
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
                    completion?.handleError(
                        "Manually created payment failed id: ${_transactionId.value.orEmpty()}"
                    )
                }
                TransactionState.SUCCESS -> {
                    completion?.handleSuccess()
                }
            }
            _transactionState.postValue(state)
        }
    }

    fun resumePayment(token: String, completion: ResumeDecisionHandler? = null) {
        val environment = environment.value!!.environment
        val body = ResumePaymentRequest(
            token
        )
        resumeRepository.create(
            _transactionId.value.orEmpty(),
            body,
            environment,
            client,
            { result ->
                _transactionResponse.postValue(result)
            },
        ) { status ->
            val state = status.toTransactionState()
            when (state) {
                TransactionState.ERROR -> {
                    completion?.handleError(
                        "Manually created payment resume failed id: ${_transactionId.value.orEmpty()}"
                    )
                }
                TransactionState.PENDING -> {
                    completion?.handleError(null)
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
                createPayment(paymentMethodToken)
            }
            paymentMethodToken.threeDSecureAuthentication != null -> _threeDsResult.postValue(
                paymentMethodToken.threeDSecureAuthentication
            )
            else -> return
        }
    }

    fun getAmountConverted(): Int {
        return AmountUtils.covert(amount.value!!, countryCode.value?.currencyCode!!)
    }
}
