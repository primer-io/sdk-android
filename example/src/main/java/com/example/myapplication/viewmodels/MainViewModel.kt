package com.example.myapplication.viewmodels

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.constants.ThemeList
import com.example.myapplication.datamodels.AppCountryCode
import com.example.myapplication.datamodels.PrimerEnv
import com.example.myapplication.datamodels.ResumePaymentRequest
import com.example.myapplication.datamodels.TransactionRequest
import com.example.myapplication.datamodels.TransactionResponse
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.datamodels.toTransactionState
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.repositories.ClientSessionRepository
import com.example.myapplication.repositories.CountryRepository
import com.example.myapplication.repositories.PaymentInstrumentsRepository
import com.example.myapplication.repositories.PaymentsRepository
import com.example.myapplication.repositories.ResumeRepository
import com.example.myapplication.utils.AmountUtils
import com.example.myapplication.utils.CombinedLiveData
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.data.settings.PrimerCardPaymentOptions
import io.primer.android.data.settings.PrimerDebugOptions
import io.primer.android.data.settings.PrimerKlarnaOptions
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerPaymentMethodOptions
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.ui.settings.PrimerUIOptions
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.ref.WeakReference
import java.util.UUID

@Keep
class MainViewModel(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource
) : ViewModel() {

    private val paymentInstrumentsRepository = PaymentInstrumentsRepository()
    private val clientSessionRepository = ClientSessionRepository(apiKeyDataSource)
    private val paymentsRepository = PaymentsRepository(apiKeyDataSource)
    private val resumeRepository = ResumeRepository(apiKeyDataSource)

    enum class Mode {
        CHECKOUT, VAULT;

        val isVaulting: Boolean get() = this == VAULT
    }

    val mode = MutableLiveData(Mode.CHECKOUT)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    private val _transactionId: MutableLiveData<String?> = MutableLiveData<String?>()

    private val _threeDsEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val threeDsEnabled: LiveData<Boolean> = _threeDsEnabled

    private val _threeDsResult: MutableLiveData<PrimerPaymentMethodTokenData.AuthenticationDetails?> =
        MutableLiveData<PrimerPaymentMethodTokenData.AuthenticationDetails?>()
    val threeDsResult: LiveData<PrimerPaymentMethodTokenData.AuthenticationDetails?> =
        _threeDsResult

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
    fun setCurrentEnv(env: PrimerEnv) {
        environment.postValue(env)
        this.apiKeyLiveData.postValue(apiKeyDataSource.getApiKey(env))
    }

    val apiKeyLiveData = MutableLiveData<String>(apiKeyDataSource
        .getApiKey(environment.value ?: PrimerEnv.Dev))
    fun setApiKeyForSelectedEnv(apiKey: String?) {
        val env = environment.value ?: return
        if (!apiKey.isNullOrBlank()) {
            this.apiKeyDataSource.setApiKey(env, apiKey)
        } else {
            this.apiKeyDataSource.setApiKey(env, null)
        }
    }

    private val _amount: MutableLiveData<Int> = MutableLiveData<Int>(10100)
    val amount: LiveData<Int> = _amount
    val amountStringified: String get() = String.format("%.2f", _amount.value!!.toDouble() / 100)
    fun setAmount(amount: Int): Unit = _amount.postValue(amount)

    private val _descriptor = MutableLiveData("Purchase: Item-123")
    val descriptor: LiveData<String> = _descriptor
    fun setDescriptor(descriptor: String) = _descriptor.postValue(descriptor)

    private val _metadata = MutableLiveData("")
    val metadata: LiveData<String> = _metadata
    fun setMetadata(metadata: String) = _metadata.postValue(metadata)

    val countryCode: MutableLiveData<AppCountryCode> = MutableLiveData(AppCountryCode.DE)
    val clientToken: MutableLiveData<String?> = MutableLiveData<String?>()
    val useStandalonePaymentMethod: MutableLiveData<PrimerPaymentMethodType> =
        MutableLiveData<PrimerPaymentMethodType>()

    val paymentInstruments: MutableLiveData<List<PrimerPaymentMethodTokenData>> =
        MutableLiveData<List<PrimerPaymentMethodTokenData>>()

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    private val _paymentHandling: MutableLiveData<PrimerPaymentHandling> = MutableLiveData()
    fun setPaymentHandling(paymentHandling: PrimerPaymentHandling): Unit = _paymentHandling.postValue(
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
            && useStandalonePaymentMethod.value == PrimerPaymentMethodType.GOOGLE_PAY
    val isStandalonePaymentMethod: Boolean
        get() = useStandalonePaymentMethod.value != null

    val settings: PrimerSettings
        get() = PrimerSettings(
            // todo: refactor to reintroduce custom values through client session
            paymentHandling = _paymentHandling.value ?: PrimerPaymentHandling.AUTO,
            paymentMethodOptions = PrimerPaymentMethodOptions(
                redirectScheme = "primer",
                cardPaymentOptions = PrimerCardPaymentOptions(threeDsEnabled.value ?: false),
                klarnaOptions = PrimerKlarnaOptions("This is custom description")
            ),
            uiOptions = PrimerUIOptions(
                isInitScreenEnabled = true,
                isSuccessScreenEnabled = true,
                isErrorScreenEnabled = true,
                theme = ThemeList.themeBySystem(contextRef.get()?.resources?.configuration),
            ),
            debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
        )

    fun configure(
        listener: PrimerCheckoutListener,
    ) {
        Primer.instance.configure(settings, listener)
    }

    fun fetchClientSession() = clientSessionRepository.fetch(
        client,
        customerId.value.orEmpty(),
        UUID.randomUUID().toString(),
        amount.value!!,
        countryRepository.getCountry().name,
        countryRepository.getCurrency(),
        environment.value?.environment ?: throw Error("no environment set!"),
        metadata.value
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
        paymentMethod: PrimerPaymentMethodTokenData,
        completion: PrimerResumeDecisionHandler? = null
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
                    completion?.continueWithNewClientToken(result.requiredAction.clientToken.orEmpty())
                } else {
                    Log.w(javaClass.simpleName, "Required actions NAME is NULL")
                }
            },
        ) { status ->
            val state = status.toTransactionState()
            when (state) {
                TransactionState.ERROR -> {
                    completion?.handleFailure(
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

    fun resumePayment(token: String, completion: PrimerResumeDecisionHandler? = null) {
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
                    completion?.handleFailure(
                        "Manually created payment resume failed id: ${_transactionId.value.orEmpty()}"
                    )
                }
                TransactionState.PENDING -> {
                    completion?.handleFailure(null)
                }
                else -> {
                    completion?.handleSuccess()
                }
            }
            _transactionState.postValue(state)
        }
    }

    // 3DS
    internal fun handleTokenData(paymentMethodToken: PrimerPaymentMethodTokenData) {
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
