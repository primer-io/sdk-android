package com.example.myapplication

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.models.ClientTokenRequest
import com.example.myapplication.models.ClientTokenResponse
import com.example.myapplication.models.CountryCode
import com.example.myapplication.models.ExampleAppRequestBody
import com.example.myapplication.models.PrimerEnv
import com.example.myapplication.models.TransactionRequest
import com.example.myapplication.models.TransactionState
import com.example.myapplication.utils.CombinedLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.primer.android.CheckoutEventListener
import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckout
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.UserDetails
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.TokenType
import io.primer.android.payment.card.Card
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.threeds.data.models.ResponseCode
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID

@Keep
class AppMainViewModel : ViewModel() {

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    private val _orderId: MutableLiveData<String> =
        MutableLiveData<String>(UUID.randomUUID().toString())
    val orderId: LiveData<String> = _orderId

    private val _threeDsEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val threeDsEnabled: LiveData<Boolean> = _threeDsEnabled

    private val _userDetails: MutableLiveData<UserDetails> = MutableLiveData<UserDetails>(
        UserDetails("Primer",
                    "Test",
                    "test@primer.io",
                    "Berlin",
                    "Address 1",
                    "Address 2",
                    "34532",
                    io.primer.android.model.dto.CountryCode.DE))
    val userDetails: LiveData<UserDetails> = _userDetails

    private val _threeDsResult: MutableLiveData<PaymentMethodToken.AuthenticationDetails?> =
        MutableLiveData<PaymentMethodToken.AuthenticationDetails?>()
    val threeDsResult: LiveData<PaymentMethodToken.AuthenticationDetails?> = _threeDsResult
    fun clearThreeDsResult(): Unit = _threeDsResult.postValue(null)

    private val _customerId: MutableLiveData<String> = MutableLiveData<String>("customer8")
    val customerId: LiveData<String> = _customerId
    fun setCustomerId(id: String): Unit = _customerId.postValue(id)

    private val _transactionResponse: MutableLiveData<String> = MutableLiveData()
    val transactionResponse: LiveData<String> = _transactionResponse

    val environment: MutableLiveData<PrimerEnv> = MutableLiveData<PrimerEnv>(PrimerEnv.Sandbox)

    private val _amount: MutableLiveData<Int> = MutableLiveData<Int>(50)
    val amount: LiveData<Int> = _amount
    val amountStringified: String get() = String.format("%.2f", _amount.value!!.toDouble() / 100)
    fun setAmount(amount: Int): Unit = _amount.postValue(amount)

    val countryCode: MutableLiveData<CountryCode> = MutableLiveData<CountryCode>(CountryCode.GB)

    private val _clientToken: MutableLiveData<String?> = MutableLiveData<String?>()
    val clientToken: LiveData<String?> = _clientToken

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)
    val transactionState: LiveData<TransactionState> = _transactionState

    fun resetTransactionState(): Unit = _transactionState.postValue(TransactionState.IDLE)

    private fun canLaunch(id: String?, amt: Int?) = !id.isNullOrEmpty() && (amt ?: 0) > 0

    val canLaunchPrimer: MutableLiveData<Boolean> =
        CombinedLiveData(customerId, _amount, ::canLaunch)

    private val _useKlarna: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val useKlarna: LiveData<Boolean> = _useKlarna
    fun setUseKlarna(use: Boolean): Unit = _useKlarna.postValue(use)

    private val _usePayPal: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val usePayPal: LiveData<Boolean> = _usePayPal
    fun setUsePayPal(use: Boolean): Unit = _usePayPal.postValue(use)

    private val _useCard: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val useCard: LiveData<Boolean> = _useCard
    fun setUseCard(use: Boolean): Unit = _useCard.postValue(use)

    private val _useGooglePay: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val useGooglePay: LiveData<Boolean> = _useGooglePay
    fun setUseGooglePay(use: Boolean): Unit = _useGooglePay.postValue(use)

    val vaultDisabled: Boolean get() = isStandalonePaymentMethod && useGooglePay.value == true

    val isStandalonePaymentMethod: Boolean
        get() = listOf(useKlarna.value, usePayPal.value, useCard.value, useGooglePay.value)
            .count { it == true } == 1

    fun generatePaymentMethodList(): List<PaymentMethod> {
        val list = mutableListOf<PaymentMethod>()
        if (useCard.value == true) list.add(Card())
        if (useKlarna.value == true) list.add(Klarna())
        if (usePayPal.value == true) list.add(PayPal())
        if (useGooglePay.value == true) list.add(GooglePay(
            merchantName = "Primer",
            totalPrice = amountStringified,
            countryCode = countryCode.value!!.toString(),
            currencyCode = countryCode.value!!.currencyCode.toString(),
        ))
        return list
    }

    private fun generateRequest(body: ExampleAppRequestBody, uri: String): Request {
        val mimeType = MediaType.get("application/json")
        val json = Gson().toJson(body)

        val reqBody = RequestBody.create(mimeType, json)

        return Request.Builder()
            .url(uri)
            .post(reqBody)
            .build()
    }

    fun fetchClientToken() {
        val body = ClientTokenRequest(customerId.value ?: return,
                                      (environment.value ?: return).environment,
                                      countryCode.value.toString())
        val request = generateRequest(body, PrimerRoutes.clientToken)

        client.cache()?.delete()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val tokenResponse = GsonBuilder()
                        .create()
                        .fromJson(response.body()?.string(), ClientTokenResponse::class.java)

                    _clientToken.postValue(tokenResponse.clientToken)
                }
            }
        })
    }

    fun createTransaction(paymentMethod: String, type: String) {
        setBusy(true)

        val amount = _amount.value ?: return
        val currencyCode = (countryCode.value ?: return).currencyCode.toString()
        val body = TransactionRequest(paymentMethod, amount, true, currencyCode)
        val request = generateRequest(body, PrimerRoutes.payments)

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                _transactionState.postValue(TransactionState.ERROR)
                setBusy(false)
            }

            override fun onResponse(call: Call, response: Response) {
                setBusy(false)
                response.use {
                    if (response.isSuccessful) {
                        _transactionResponse.postValue(response.body()?.string())
                        _transactionState.postValue(TransactionState.SUCCESS)
                    } else {
                        _transactionResponse.postValue(response.body()?.string())
                        _transactionState.postValue(TransactionState.ERROR)
                    }
                }
            }
        })
    }

    private val _isBusy: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isBusy: LiveData<Boolean> = _isBusy
    internal fun setBusy(busy: Boolean) = _isBusy.postValue(busy);

    fun fetchSavedPaymentMethods() {
        setBusy(true)
        UniversalCheckout.getSavedPaymentMethods { tokens ->
            setBusy(false)
            _vaultedPaymentTokens.postValue(tokens)
        }
    }

    private val _vaultedPaymentTokens: MutableLiveData<List<PaymentMethodToken>> =
        MutableLiveData<List<PaymentMethodToken>>()
    val vaultedPaymentTokens: LiveData<List<PaymentMethodToken>> = _vaultedPaymentTokens

    val listener: CheckoutEventListener = object : CheckoutEventListener {
        override fun onCheckoutEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.TokenizationSuccess -> {
                    UniversalCheckout.dismiss(false)
                    println("🚀🚀🚀 token event: ${e.data}")
                    handleTokenData(e.data)
                }
                is CheckoutEvent.TokenAddedToVault -> {
                }
                is CheckoutEvent.ApiError -> {
                    UniversalCheckout.dismiss()
                }
                is CheckoutEvent.Exit -> {
                    fetchSavedPaymentMethods()
                }
                is CheckoutEvent.TokenSelected -> {
                    createTransaction(e.data.token, e.data.paymentInstrumentType)
                }
                else -> return
            }
        }
    }

    internal fun handleTokenData(paymentMethodToken: PaymentMethodToken) {
        when {
            paymentMethodToken.tokenType == TokenType.SINGLE_USE
                    && (paymentMethodToken.threeDSecureAuthentication?.responseCode == ResponseCode.AUTH_SUCCESS
                    || paymentMethodToken.threeDSecureAuthentication == null) -> {
                createTransaction(paymentMethodToken.token,
                                  paymentMethodToken.paymentInstrumentType)
            }
            paymentMethodToken.threeDSecureAuthentication != null -> _threeDsResult.postValue(
                paymentMethodToken.threeDSecureAuthentication)
            else -> return
        }
    }
}
