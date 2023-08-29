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
import com.snowballtech.transit.rta.module.transit.TransitPhysicalCard
import io.primer.android.BuildConfig
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.core.composable.Collectable
import io.primer.android.components.manager.core.composable.Errorable
import io.primer.android.components.manager.core.composable.PrimerHeadlessManager
import io.primer.android.components.manager.core.composable.Submitable
import io.primer.android.components.manager.core.composable.Validatable
import io.primer.android.components.manager.nolPay.composable.NfcScannable
import io.primer.android.components.manager.nolPay.composable.Stepable
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DIAppComponent
import io.primer.android.di.NOL_PAY_ERROR_RESOLVER_NAME
import io.primer.android.domain.base.BaseErrorFlowResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get
import org.koin.core.qualifier.named

enum class NolPayStep {

    COLLECT_PHONE_DATA,
    COLLECT_OTP_DATA,
    COLLECT_TAG_DATA
}

sealed interface NolPayData {

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryCode: String) : NolPayData
    data class NolPayOtpData(val otpCode: String) : NolPayData
    data class NolPayTagData(val tag: Tag) : NolPayData
}

class PrimerHeadlessNolPayManager internal constructor(
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayGetLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val nolPayGetLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val nolPayLinkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val errorFlowResolver: BaseErrorFlowResolver,
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
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "e57ba94ef11c4a07becb322eaec1bdd0" }
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
            // todo extract logic in different delegates
            when (val collectedData = collectedData.replayCache.last()) {
                is NolPayData.NolPayTagData -> {
//                        savedStateHandle[PHYSICAL_CARD_KEY] =
//                            Transit.getTransitInstance().getPhysicalCard(
//                                TransitGetPhysicalCardRequest.Builder().setTag(collectedData.tag)
//                                    .build()
//                            )
                    // comment out when unlinking
                    nolPayGetLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
                        .onSuccess { linkToken ->
                            savedStateHandle[LINKED_TOKEN_KEY] = linkToken
                            _stepFlow.emit(NolPayStep.COLLECT_PHONE_DATA)
                        }.onFailure { throwable ->
                           errorFlowResolver.resolve(throwable, _errorFlow)
                        }


                    // uncomment to unlink
//                    launch(Dispatchers.IO) {
//                        runSuspendCatching {
//                            Transit.getPaymentInstance().getUnlinkPaymentCardOTP(
//                                TransitUnlinkPaymentCardOTPRequest.Builder().setMobile("45678901")
//                                    .setRegionCode("971").setCardNumber("313971137").build()
//                            )
//                        }.onSuccess { cardToken ->
//                            runSuspendCatching {
//                                Transit.getPaymentInstance().unlinkPaymentCard(
//                                    TransitUnlinkPaymentCardRequest.Builder()
//                                        .setCardNumber(cardToken.cardNumber!!)
//                                        .setOTPCode("987123")
//                                        .setUnlinkPaymentCardToken(cardToken.unlinkToken!!).build()
//                                )
//                            }
//                        }
//                    }
                }

                is NolPayData.NolPayPhoneData -> {
                    nolPayGetLinkPaymentCardOTPInteractor(
                        NolPayCardOTPParams(
                            collectedData.mobileNumber,
                            collectedData.phoneCountryCode,
                            requireNotNull(savedStateHandle.get<String>(LINKED_TOKEN_KEY))
                        )
                    ).onSuccess {
                        savedStateHandle[REGION_CODE_KEY] = collectedData.phoneCountryCode
                        savedStateHandle[MOBILE_NUMBER_KEY] = collectedData.mobileNumber
                        _stepFlow.emit(NolPayStep.COLLECT_OTP_DATA)
                    }.onFailure { throwable ->
                        errorFlowResolver.resolve(throwable, _errorFlow)
                    }
                }

                is NolPayData.NolPayOtpData -> {
                    nolPayLinkPaymentCardInteractor(
                        NolPayLinkCardParams(
                            collectedData.otpCode,
                            requireNotNull(savedStateHandle.get<String>(LINKED_TOKEN_KEY))
                        )
                    ).onSuccess {
                        tokenize()
                    }.onFailure { throwable ->
                        errorFlowResolver.resolve(throwable, _errorFlow)
                    }
                }
            }
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

    private fun tokenize() = viewModelScope.launch {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                NolPayPaymentInstrumentParams(
                    PaymentMethodType.NOL_PAY.name,
                    Transit.getId(),
                    requireNotNull(savedStateHandle.get<String>(REGION_CODE_KEY)),
                    requireNotNull(savedStateHandle.get<String>(MOBILE_NUMBER_KEY)),
                    requireNotNull(
                        savedStateHandle.get<TransitPhysicalCard>(
                            PHYSICAL_CARD_KEY
                        )?.cardNumber
                    )
                ),
                PrimerSessionIntent.VAULT
            )
        ).collect()
    }

    companion object : DIAppComponent {

        private const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        private const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
        private const val REGION_CODE_KEY = "REGION_CODE"
        private const val MOBILE_NUMBER_KEY = "MOBILE_NUMBER"

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
                            get(),
                            get(),
                            get(),
                            get(named(NOL_PAY_ERROR_RESOLVER_NAME)),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[PrimerHeadlessNolPayManager::class.java]
        }
    }
}
