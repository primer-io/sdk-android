package io.primer.android.components.manager.nolPay

import android.app.Activity
import android.content.Intent
import android.nfc.Tag
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import com.snowballtech.transit.rta.configuration.TransitConfiguration
import com.snowballtech.transit.rta.module.payment.TransitPayRequest
import com.snowballtech.transit.rta.module.payment.TransitPaymentCardListRequest
import com.snowballtech.transit.rta.module.payment.TransitPaymentCardListResponse
import io.primer.android.BuildConfig
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.data.payments.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.core.composable.Collectable
import io.primer.android.components.manager.core.composable.Errorable
import io.primer.android.components.manager.core.composable.PrimerHeadlessManager
import io.primer.android.components.manager.core.composable.Submitable
import io.primer.android.components.manager.core.composable.Validatable
import io.primer.android.components.manager.nolPay.composable.NfcScannable
import io.primer.android.components.manager.nolPay.composable.Stepable
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

enum class NolPayStep {

    COLLECT_PHONE_DATA,
    COLLECT_OTP_DATA,
    COLLECT_TAG_DATA,
    PAYMENT_TOKENIZED
}

sealed interface NolPayData {

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryCode: String) : NolPayData
    data class NolPayOtpData(val otpCode: String) : NolPayData
    data class NolPayTagData(val tag: Tag) : NolPayData
}

class PrimerHeadlessNolPayManager internal constructor(
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val nolPayLinkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessManager,
    Collectable<NolPayData>,
    Stepable<NolPayStep>,
    Errorable,
    NfcScannable,
    Validatable,
    Submitable {

    private val _stepFlow: MutableSharedFlow<NolPayStep> = MutableSharedFlow()
    override val stepFlow: SharedFlow<NolPayStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayData> =
        MutableSharedFlow(replay = 1)
    override val collectedData: SharedFlow<NolPayData> = _collectedData

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "93f9324ef6424dbaaabf47290a567db7" }
            }
        }
    }

    override fun start(sessionIntent: PrimerSessionIntent) {
        viewModelScope.launch {
            nolPayConfigurationInteractor(None()).collectLatest { configuration ->
                initSDK(configuration)
            }
        }
    }

    override fun updateCollectedData(t: NolPayData) {
        viewModelScope.launch { _collectedData.emit(t) }
        viewModelScope.launch {
            _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        viewModelScope.launch {
            nolPayLinkPaymentCardDelegate.handleCollectedCardData(
                collectedData.replayCache.last(),
                _stepFlow,
                _errorFlow,
                savedStateHandle
            )
        }
    }

    override fun enableForegroundDispatch(
        activity: Activity,
        requestCode: Int
    ) = Transit.getPaymentInstance().enableForegroundDispatch(activity, requestCode)

    override fun disableForegroundDispatch(
        activity: Activity,
    ) = Transit.getPaymentInstance().disableForegroundDispatch(activity)

    override fun getAvailableTag(intent: Intent?): Tag? {
        return Transit.getPaymentInstance().getAvailableTag(intent)
    }

    suspend fun getLinkedCards(phoneData: NolPayData.NolPayPhoneData) = viewModelScope.launch(Dispatchers.IO) {
        runSuspendCatching {
            val cards = Transit.getPaymentInstance()
                .getPaymentCardList(
                    TransitPaymentCardListRequest.Builder().setMobile(phoneData.mobileNumber)
                        .setRegionCode(phoneData.phoneCountryCode).build()
                )
        }
    }

    fun createPayment(t: NolPayData.NolPayTagData) = viewModelScope.launch(Dispatchers.IO) {
        Transit.getPaymentInstance().requestPayment(
            TransitPayRequest.Builder().setTransactionNo("1698959181543133186").setTag(t.tag)
                .build()
        )
    }

    private fun initSDK(configuration: NolPayConfiguration) {
        TransitConfiguration.Builder()
            .enableSandbox(configuration.environment != Environment.PRODUCTION)
            .enableDebug(BuildConfig.DEBUG)
            .setAppID(configuration.merchantAppId)
            .setAppSecretKeyHandler(handler)
            .build().apply {
                Transit.initSDK(this)
            }

        viewModelScope.launch {
            _stepFlow.emit(NolPayStep.COLLECT_TAG_DATA)
        }
    }

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): PrimerHeadlessNolPayManager {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return PrimerHeadlessNolPayManager(
                            get(),
                            get(),
                            get(),
                            get(),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[PrimerHeadlessNolPayManager::class.java]
        }
    }
}
