package io.primer.android.components.manager.nolPay.linkCard.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.snowballtech.transit.rta.configuration.TransitAppSecretKeyHandler
import io.primer.android.BuildConfig
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStepable
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.base.None
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

class NolPayLinkCardComponent internal constructor(
    private val appSecretInteractor: NolPayAppSecretInteractor,
    private val configurationInteractor: NolPayConfigurationInteractor,
    private val linkPaymentCardDelegate: NolPayLinkPaymentCardDelegate,
    private val dataValidatorRegistry: NolPayDataValidatorRegistry,
    private val analyticsInteractor: AnalyticsInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    PrimerHeadlessCollectDataComponent<NolPayLinkCollectableData>,
    PrimerHeadlessStepable<NolPayLinkCardStep>,
    PrimerHeadlessStartable {

    private val handler = object : TransitAppSecretKeyHandler {
        override fun getAppSecretKeyFromServer(sdkId: String): String {
            return runBlocking {
                appSecretInteractor(NolPaySecretParams(sdkId)).getOrElse { "2675dcb9cc034bddbd1ad48908840542" }
            }
        }
    }

    private val _stepFlow: MutableSharedFlow<NolPayLinkCardStep> = MutableSharedFlow()
    override val stepFlow: Flow<NolPayLinkCardStep> = _stepFlow

    private val _errorFlow: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val errorFlow: SharedFlow<PrimerError> = _errorFlow

    private val _validationFlow: MutableSharedFlow<List<PrimerValidationError>> =
        MutableSharedFlow()
    override val validationFlow: SharedFlow<List<PrimerValidationError>> = _validationFlow

    private val _collectedData: MutableSharedFlow<NolPayLinkCollectableData> =
        MutableSharedFlow(replay = 1)

    override fun updateCollectedData(collectedData: NolPayLinkCollectableData) {
        logSdkFunctionCalls(
            NolPayAnalyticsConstants.LINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
            mapOf(NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectedData.toString())
        )
        viewModelScope.launch { _collectedData.emit(collectedData) }
        viewModelScope.launch {
            _validationFlow.emit(
                dataValidatorRegistry.getValidator(collectedData).validate(collectedData)
            )
        }
    }

    override fun submit() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_SUBMIT_DATA_METHOD)
        viewModelScope.launch {
            linkPaymentCardDelegate.handleCollectedCardData(
                _collectedData.replayCache.last(),
                savedStateHandle
            ).onSuccess { step ->
                _stepFlow.emit(step)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun start() {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINK_CARD_START_METHOD)
        viewModelScope.launch {
            configurationInteractor(None()).collectLatest { configuration ->
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
            _stepFlow.emit(NolPayLinkCardStep.CollectTagData)
        }
    }.onFailure {
        it.printStackTrace()
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

    companion object : DIAppComponent {
        fun getInstance(owner: ViewModelStoreOwner): NolPayLinkCardComponent {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return NolPayLinkCardComponent(
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            extras.createSavedStateHandle()
                        ) as T
                    }
                }
            )[NolPayLinkCardComponent::class.java]
        }
    }
}
