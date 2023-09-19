package io.primer.android.components.manager.nolPay.unlinkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.BuildConfig
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.error.DefaultErrorMapperFactory
import io.primer.android.di.DIAppComponent
import io.primer.android.di.NOL_PAY_ERROR_RESOLVER_NAME
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get
import org.koin.core.qualifier.named

class NolPayUnlinkCardComponent internal constructor(
    private val nolPayAppSecretInteractor: NolPayAppSecretInteractor,
    private val nolPayConfigurationInteractor: NolPayConfigurationInteractor,
    private val unlinkPaymentCardDelegate: NolPayUnlinkPaymentCardDelegate,
    private val nolPayDataValidatorRegistry: NolPayDataValidatorRegistry,
    private val analyticsInteractor: AnalyticsInteractor,
    private val errorMapper: ErrorMapper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayUnlinkCollectableData>,
    PrimerHeadlessStepable<NolPayUnlinkCardStep>,
    PrimerHeadlessStartable {

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                nolPayAppSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "32893fc5f6be4a5b95cbd7bbcceffd56" }
            }
        }
    }

    private val _stepFlow: MutableSharedFlow<NolPayUnlinkCardStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayUnlinkCardStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayUnlinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun updateCollectedData(collectedData: NolPayUnlinkCollectableData) {
        logSdkFunctionCalls(
            NolPayAnalyticsConstants.UNLINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
            mapOf(NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectedData.toString())
        )
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            //   _validationFlow.emit(nolPayDataValidatorRegistry.getValidator(t).validate(t))
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
                savedStateHandle
            ).onSuccess {
                _stepFlow.emit(it)
            }.onFailure {
                errorMapper.getPrimerError(it)
                    .also { error ->
                        _errorFlow.emit(error)
                    }.also { error ->
                        logSdkErrors(error)
                    }
            }
        }
    }

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.UNLINK_CARD_START_METHOD)
        viewModelScope.launch {
            nolPayConfigurationInteractor(None()).collectLatest { configuration ->
                initSDK(configuration)
            }
        }
    }

    private suspend fun initSDK(configuration: NolPayConfiguration) = runSuspendCatching {
        PrimerNolPay.initSDK(
            configuration.environment != Environment.PRODUCTION,
            BuildConfig.DEBUG,
            configuration.merchantAppId,
            handler
        )
    }.onSuccess {
        viewModelScope.launch {
            _stepFlow.emit(NolPayUnlinkCardStep.CollectCardData)
        }
    }.onFailure {
        errorMapper.getPrimerError(it)
            .also { error ->
                _errorFlow.emit(error)
            }.also { error ->
                logSdkErrors(error)
            }
    }

    private fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = viewModelScope.launch {
        analyticsInteractor(
            SdkFunctionParams(
                methodName,
                mapOf(
                    "category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name
                ).plus(context)
            )
        ).collect()
    }

    private fun logSdkErrors(
        error: PrimerError,
    ) = viewModelScope.launch {
        analyticsInteractor(
            MessageAnalyticsParams(
                MessageType.ERROR,
                error.description,
                Severity.ERROR,
                error.diagnosticsId
            )
        ).collect()
    }

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return NolPayUnlinkCardComponent(
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
            )[NolPayUnlinkCardComponent::class.java]
        }
    }
}
