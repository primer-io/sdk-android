package io.primer.android.components.manager.banks.component

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.PrimerSessionIntent
import io.primer.android.banks.di.BankWebRedirectComposer
import io.primer.android.banks.implementation.errors.data.exception.BanksIllegalValueKey
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.presentation.delegate.GetBanksDelegate
import io.primer.android.banks.implementation.tokenization.presentation.model.BankIssuerTokenizationInputable
import io.primer.android.banks.implementation.validation.validator.BankIdValidator
import io.primer.android.banks.implementation.validation.validator.banksNotLoadedPrimerValidationError
import io.primer.android.components.manager.banks.analytics.BanksAnalyticsConstants
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.debounce
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("complexity:LongParameterList", "detekt:ForbiddenComment")
internal class DefaultBanksComponent(
    private val paymentMethodType: String,
    private val redirectComposer: BankWebRedirectComposer,
    private val getBanksDelegate: GetBanksDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val savedStateHandle: SavedStateHandle,
    private val onFinished: () -> Unit,
) : ViewModel(), BanksComponent {
    @VisibleForTesting
    var banks: List<IssuingBank>? = null

    private var bankId: String? = savedStateHandle["bank_id"]
        set(value) {
            savedStateHandle["bank_id"] = value
            field = value
        }

    private val _componentStep = MutableSharedFlow<BanksStep>()
    override val componentStep: Flow<BanksStep> = _componentStep

    private val _componentError = MutableSharedFlow<PrimerError>()
    override val componentError: Flow<PrimerError> = _componentError

    private val _componentValidationStatus =
        MutableSharedFlow<PrimerValidationStatus<BanksCollectableData>>()
    override val componentValidationStatus: Flow<PrimerValidationStatus<BanksCollectableData>> =
        _componentValidationStatus

    private val onCollectableDataUpdated: (BanksCollectableData) -> Unit =
        viewModelScope.debounce { collectedData ->
            when (collectedData) {
                is BanksCollectableData.Filter -> onCollectFilter(collectedData)

                is BanksCollectableData.BankId -> onCollectBankId(collectedData)
            }
        }

    override fun start() {
        viewModelScope.launch {
            launch {
                getBanks(query = null)
            }
            launch {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = BanksAnalyticsConstants.BANKS_START_METHOD,
                    paymentMethodType = paymentMethodType,
                )
            }
            launch {
                redirectComposer.start()
            }
        }
    }

    override fun updateCollectedData(collectedData: BanksCollectableData) {
        viewModelScope.launch {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                paymentMethodType = paymentMethodType,
            )
        }
        onCollectableDataUpdated(collectedData)
    }

    override fun submit() {
        viewModelScope.launch {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_SUBMIT_DATA_METHOD,
                paymentMethodType = paymentMethodType,
            )

            runSuspendCatching {
                requireNotNullCheck(value = bankId, key = BanksIllegalValueKey.BANK_ID)
            }.flatMap { bankId ->
                redirectComposer.startPaymentFlow(
                    inputable =
                        BankIssuerTokenizationInputable(
                            paymentMethodType = paymentMethodType,
                            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                            bankIssuer = bankId,
                        ),
                )
            }
                .onSuccess { onFinished() }
                .onFailure(::handleError)
        }
    }

    // region Utils
    private suspend inline fun getBanks(query: String?) {
        _componentStep.emit(BanksStep.Loading)
        getBanksDelegate.getBanks(query = query)
            .onSuccess { banks ->
                this.banks = banks
                _componentStep.emit(BanksStep.BanksRetrieved(banks = banks))
            }
            .onFailure(::handleError)
    }

    private suspend fun onCollectFilter(filter: BanksCollectableData.Filter) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = filter),
        )
        if (banks == null) {
            validationErrorLoggingDelegate.logSdkAnalyticsError(banksNotLoadedPrimerValidationError)
            _componentValidationStatus.emit(
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(banksNotLoadedPrimerValidationError),
                    collectableData = filter,
                ),
            )
        } else {
            _componentValidationStatus.emit(
                PrimerValidationStatus.Valid(collectableData = filter),
            )
            getBanks(query = filter.text)
        }
    }

    private suspend fun onCollectBankId(bankId: BanksCollectableData.BankId) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = bankId),
        )

        val banks = this.banks

        val validationError =
            BankIdValidator.validate(
                banks = banks,
                bankId = bankId.id,
            )

        _componentValidationStatus.emit(
            if (validationError != null) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(validationError),
                    collectableData = bankId,
                )
            } else {
                this.bankId = bankId.id
                PrimerValidationStatus.Valid(collectableData = bankId)
            },
        )
    }

    private fun handleError(throwable: Throwable) =
        viewModelScope.launch {
            errorMapperRegistry.getPrimerError(throwable)
                .also { error ->
                    _componentError.emit(error)
                    errorLoggingDelegate.logSdkAnalyticsErrors(error = error)
                }
        }
    // endregion
}
