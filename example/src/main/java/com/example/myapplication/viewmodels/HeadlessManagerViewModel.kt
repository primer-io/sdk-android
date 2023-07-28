package com.example.myapplication.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.myapplication.datamodels.PrimerEnv
import com.example.myapplication.datamodels.ResumePaymentRequest
import com.example.myapplication.datamodels.TransactionRequest
import com.example.myapplication.datamodels.TransactionResponse
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.datamodels.toTransactionState
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.repositories.PaymentsRepository
import com.example.myapplication.repositories.ResumeRepository
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.PrimerHeadlessUniversalCheckoutUiListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class HeadlessManagerViewModel(
    apiKeyDataSource: ApiKeyDataSource,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessUniversalCheckoutListener,
    PrimerHeadlessUniversalCheckoutUiListener {

    private val headlessUniversalCheckout: PrimerHeadlessUniversalCheckoutInterface =
        PrimerHeadlessUniversalCheckout.current

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    private val paymentsRepository = PaymentsRepository(apiKeyDataSource)
    private val resumeRepository = ResumeRepository(apiKeyDataSource)

    private val _transactionId: MutableLiveData<String?> = MutableLiveData<String?>()
    private val _transactionResponse: MutableLiveData<TransactionResponse> = MutableLiveData()
    val transactionResponse: LiveData<TransactionResponse> = _transactionResponse
    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    fun resetUiState() {
        _uiState.value = UiState.InitializedHeadless
    }

    private val _paymentMethodsLoaded =
        MutableLiveData<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>()
    val paymentMethodsLoaded: LiveData<List<PrimerHeadlessUniversalCheckoutPaymentMethod>> =
        _paymentMethodsLoaded

    var isLaunched: Boolean?
        get() = savedStateHandle.get<Boolean>(IS_LAUNCHED_KEY)
        set(value) {
            savedStateHandle[IS_LAUNCHED_KEY] = value
        }

    fun startHeadless(context: Context, clientToken: String, settings: PrimerSettings) {
        _uiState.postValue(UiState.InitializingHeadless)
        headlessUniversalCheckout.start(
            context,
            clientToken,
            settings,
            this,
            this
        )
    }

    fun setHeadlessListeners() {
        headlessUniversalCheckout.setCheckoutListener(this)
        headlessUniversalCheckout.setCheckoutUiListener(this)
    }

    fun createPayment(
        paymentMethod: PrimerPaymentMethodTokenData,
        environment: PrimerEnv,
        descriptor: String?,
        completion: PrimerHeadlessUniversalCheckoutResumeDecisionHandler? = null
    ) {
        _transactionId.postValue(null)

        val environment = environment.environment
        val body = TransactionRequest.create(
            paymentMethod.token,
            descriptor.orEmpty()
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
            _transactionState.postValue(state)
        }
    }

    fun resumePayment(
        token: String,
        environment: PrimerEnv,
        completion: PrimerHeadlessUniversalCheckoutResumeDecisionHandler? = null
    ) {
        val environment = environment.environment
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
            _transactionState.postValue(state)
        }
    }

    override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        _uiState.postValue(UiState.InitializedHeadless)
        _paymentMethodsLoaded.value = paymentMethods
    }

    override fun onTokenizationStarted(paymentMethodType: String) {
        _uiState.value = UiState.TokenizationStarted(paymentMethodType)
    }

    override fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) {
        _uiState.postValue(
            UiState.TokenizationSuccessReceived(paymentMethodTokenData, decisionHandler)
        )
    }

    override fun onCheckoutResume(
        resumeToken: String,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) {
        _uiState.postValue(UiState.ResumePaymentReceived(resumeToken, decisionHandler))
    }

    override fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) {
        super.onResumePending(additionalInfo)
        _uiState.value = UiState.ResumePendingReceived(additionalInfo)
    }

    override fun onCheckoutAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) {
        super.onCheckoutAdditionalInfoReceived(additionalInfo)
        _uiState.value = UiState.AdditionalInfoReceived(additionalInfo)
    }

    override fun onBeforePaymentCreated(
        paymentMethodData: PrimerPaymentMethodData,
        createPaymentHandler: PrimerPaymentCreationDecisionHandler
    ) {
        super.onBeforePaymentCreated(paymentMethodData, createPaymentHandler)
        _uiState.value = UiState.BeforePaymentCreateReceived(paymentMethodData)
    }

    override fun onFailed(error: PrimerError) {
        _uiState.value = UiState.ShowError(null, error)
    }

    override fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData?) {
        _uiState.value = UiState.ShowError(checkoutData?.payment, error)
    }

    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
        _uiState.value = UiState.CheckoutCompleted(checkoutData)
    }

    override fun onBeforeClientSessionUpdated() {
        super.onBeforeClientSessionUpdated()
        Log.d(TAG, "onBeforeClientSessionUpdated")
        _uiState.value = UiState.BeforeClientSessionUpdateReceived
    }

    override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
        super.onClientSessionUpdated(clientSession)
        Log.d(TAG, "onClientSessionUpdated")
        _uiState.value = UiState.ClientSessionUpdatedReceived
    }

    override fun onPreparationStarted(paymentMethodType: String) {
        _uiState.value = UiState.PreparationStarted(paymentMethodType)
    }

    override fun onPaymentMethodShowed(paymentMethodType: String) {
        _uiState.value = UiState.PaymentMethodShowed(paymentMethodType)
    }

    override fun onCleared() {
        super.onCleared()
        headlessUniversalCheckout.cleanup()
    }

    companion object {
        private val TAG = this::class.simpleName
        private const val IS_LAUNCHED_KEY = "LAUNCHED"
    }
}

sealed class UiState {
    object InitializingHeadless : UiState()
    object InitializedHeadless : UiState()
    data class TokenizationStarted(val paymentMethodType: String) : UiState()
    data class PreparationStarted(val paymentMethodType: String) : UiState()
    data class PaymentMethodShowed(val paymentMethodType: String) : UiState()
    data class TokenizationSuccessReceived(
        val paymentMethodTokenData: PrimerPaymentMethodTokenData,
        val decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) : UiState()

    data class ResumePaymentReceived(
        val resumeToken: String,
        val decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) : UiState()

    data class ResumePendingReceived(val additionalInfo: PrimerCheckoutAdditionalInfo) : UiState()
    data class AdditionalInfoReceived(val additionalInfo: PrimerCheckoutAdditionalInfo) : UiState()
    data class BeforePaymentCreateReceived(val paymentMethodData: PrimerPaymentMethodData) :
        UiState()

    object BeforeClientSessionUpdateReceived : UiState()
    object ClientSessionUpdatedReceived : UiState()

    data class ShowError(val payment: Payment?, val error: PrimerError) : UiState()
    data class CheckoutCompleted(val checkoutData: PrimerCheckoutData) : UiState()
}
