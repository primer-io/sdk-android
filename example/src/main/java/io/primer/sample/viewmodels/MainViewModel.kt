package io.primer.sample.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.data.settings.PrimerDebugOptions
import io.primer.android.data.settings.PrimerGooglePayOptions
import io.primer.android.data.settings.PrimerKlarnaOptions
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerPaymentMethodOptions
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.data.settings.PrimerThreeDsOptions
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.ui.settings.PrimerUIOptions
import io.primer.sample.BuildConfig
import io.primer.sample.constants.ThemeList
import io.primer.sample.datamodels.AppCountryCode
import io.primer.sample.datamodels.PrimerEnv
import io.primer.sample.datamodels.ResumePaymentRequest
import io.primer.sample.datamodels.TransactionRequest
import io.primer.sample.datamodels.TransactionResponse
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toTransactionState
import io.primer.sample.datasources.ApiKeyDataSource
import io.primer.sample.repositories.ClientSessionRepository
import io.primer.sample.repositories.CountryRepository
import io.primer.sample.repositories.PaymentsRepository
import io.primer.sample.repositories.ResumeRepository
import io.primer.sample.utils.CombinedLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.UUID

@Keep
class MainViewModel(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientSessionRepository = ClientSessionRepository(apiKeyDataSource)
    private val paymentsRepository = PaymentsRepository(apiKeyDataSource)
    private val resumeRepository = ResumeRepository(apiKeyDataSource)

    enum class SelectedFlow {
        CREATE_SESSION,
        CLIENT_TOKEN
    }

    enum class Mode {
        CHECKOUT, VAULT;
    }

    val mode = MutableLiveData(Mode.CHECKOUT)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val _transactionId: MutableLiveData<String?> = MutableLiveData<String?>()

    private val _selectedFlow = MutableLiveData<SelectedFlow>()
    val selectedFlow: LiveData<SelectedFlow> = _selectedFlow
    fun setSelectedFlow(flow: SelectedFlow) = _selectedFlow.postValue(flow)

    private val _clientToken = savedStateHandle.getLiveData<String?>("token")
    val clientToken: LiveData<String?> = _clientToken
    fun setClientToken(token: String?) = _clientToken.postValue(token.takeIf {
        it.isNullOrBlank().not()
    })

    private val _payAfterVaulting = MutableLiveData<Boolean>()
    val payAfterVaulting: LiveData<Boolean> = _payAfterVaulting
    fun setPayAfterVaulting(payAfterVault: Boolean) = _payAfterVaulting.postValue(payAfterVault)

    private val _customerId: MutableLiveData<String> = MutableLiveData<String>("customer8")
    val customerId: LiveData<String> = _customerId
    fun setCustomerId(id: String): Unit =
        _customerId.postValue(id)

    private val _transactionResponse: MutableLiveData<TransactionResponse> = MutableLiveData()
    val transactionResponse: LiveData<TransactionResponse> = _transactionResponse

    val environment: MutableLiveData<PrimerEnv> = MutableLiveData<PrimerEnv>(PrimerEnv.Sandbox)
    fun setCurrentEnv(env: PrimerEnv) {
        environment.postValue(env)
        this.apiKeyLiveData.postValue(apiKeyDataSource.getApiKey(env))
    }

    val apiKeyLiveData = MutableLiveData<String>(
        apiKeyDataSource
            .getApiKey(environment.value ?: PrimerEnv.Dev)
    )

    fun setApiKeyForSelectedEnv(apiKey: String?) {
        val env = environment.value ?: return
        if (!apiKey.isNullOrBlank()) {
            this.apiKeyDataSource.setApiKey(env, apiKey)
        } else {
            this.apiKeyDataSource.setApiKey(env, null)
        }
    }

    private val _amount: MutableLiveData<Int> = MutableLiveData<Int>(1000)
    val amount: LiveData<Int> = _amount
    val amountStringified: String get() = String.format(Locale.US, "%.2f", _amount.value!!.toDouble() / 100)
    fun setAmount(amount: Int): Unit = _amount.postValue(amount)

    private val _descriptor = MutableLiveData("Purchase: Item-123")
    val descriptor: LiveData<String> = _descriptor
    fun setDescriptor(descriptor: String) = _descriptor.postValue(descriptor)

    private val _metadata = MutableLiveData("")
    val metadata: LiveData<String> = _metadata
    fun setMetadata(metadata: String) = _metadata.postValue(metadata)

    private val _captureVaultedCardCvv = MutableLiveData(false)
    val captureVaultedCardCvv: LiveData<Boolean> = _captureVaultedCardCvv
    fun setCaptureVaultedCardCvv(captureVaultedCardCvv: Boolean) = _captureVaultedCardCvv.postValue(
        captureVaultedCardCvv
    )

    val countryCode: MutableLiveData<AppCountryCode> = MutableLiveData(AppCountryCode.DE)

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    private val _paymentHandling: MutableLiveData<PrimerPaymentHandling> = MutableLiveData()
    fun setPaymentHandling(paymentHandling: PrimerPaymentHandling): Unit = _paymentHandling.postValue(
        paymentHandling
    )

    private val _uiOptions: MutableLiveData<PrimerUIOptions> =
        MutableLiveData(PrimerUIOptions(theme = ThemeList.themeBySystem(contextRef.get()?.resources?.configuration)))

    fun setInitScreenUiOptions(enabled: Boolean): Unit = _uiOptions.postValue(
        _uiOptions.value?.copy(isInitScreenEnabled = enabled)
    )

    val useStandalonePaymentMethod: MutableLiveData<String> = MutableLiveData()

    fun resetTransactionState(): Unit =
        _transactionState.postValue(TransactionState.IDLE)

    private fun canLaunch(id: String?, amt: Int?) =
        !id.isNullOrEmpty() && amt != null

    val canLaunchPrimer: MutableLiveData<Boolean> =
        CombinedLiveData(customerId, _amount, ::canLaunch)

    val settings: PrimerSettings
        get() = PrimerSettings(
            // todo: refactor to reintroduce custom values through client session
            paymentHandling = _paymentHandling.value ?: PrimerPaymentHandling.AUTO,
            paymentMethodOptions = PrimerPaymentMethodOptions(
                redirectScheme = "primer",
                klarnaOptions = PrimerKlarnaOptions(
                    recurringPaymentDescription = "This is custom description",
                    returnIntentUrl = Uri.Builder()
                        .scheme("app")
                        .authority("deeplink.return.activity")
                        .build()
                        .toString()
                ),
                googlePayOptions = PrimerGooglePayOptions(
                    captureBillingAddress = true,
                    existingPaymentMethodRequired = false,
//                    shippingAddressParameters = PrimerGoogleShippingAddressParameters(
//                        phoneNumberRequired = true
//                    ),
//                    emailAddressRequired = true,
//                    requireShippingMethod = true
                ),
                threeDsOptions = PrimerThreeDsOptions("https://primer.io/3ds"),
                stripeOptions = PrimerStripeOptions(
                    mandateData = PrimerStripeOptions.MandateData.TemplateMandateData("Primer Inc."),
                    publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
                )
            ),
            uiOptions = _uiOptions.value ?: PrimerUIOptions(),
            debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
            clientSessionCachingEnabled = true
        )

    private val _collectedTag: MutableLiveData<Intent?> = MutableLiveData()
    val collectedTag: LiveData<Intent?> = _collectedTag
    fun setTag(intent: Intent?) = _collectedTag.postValue(intent)

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
        metadata.value,
        _captureVaultedCardCvv.value ?: false
    ) { t ->
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                savedStateHandle["token"] = t
            }
        }
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
                    completion?.continueWithNewClientToken(
                        result.requiredAction.clientToken.orEmpty()
                    )
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

                else -> Unit
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
}
