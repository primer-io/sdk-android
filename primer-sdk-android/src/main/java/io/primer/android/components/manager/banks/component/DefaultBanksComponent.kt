@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package io.primer.android.components.manager.banks.component

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.components.data.payments.paymentMethods.componentWithRedirect.banks.exception.BanksIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.banks.validation.validator.BankIdValidator
import io.primer.android.components.domain.payments.paymentMethods.banks.validation.validator.banksNotLoadedPrimerValidationError
import io.primer.android.components.manager.banks.analytics.BanksAnalyticsConstants
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.banks.di.BanksComponentProvider
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.BankIssuerTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.GetBanksDelegate
import io.primer.android.core.extensions.debounce
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("complexity:LongParameterList", "detekt:ForbiddenComment")
internal class DefaultBanksComponent constructor(
    private val paymentMethodType: String,
    private val getBanksDelegate: GetBanksDelegate,
    private val bankIssuerTokenizationDelegate: BankIssuerTokenizationDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    private val errorMapper: ErrorMapper,
    private val savedStateHandle: SavedStateHandle,
    private val onFinished: () -> Unit
) : ViewModel(), BanksComponent {
    @VisibleForTesting
    var banks: List<IssuingBank>? = null

    private var bankId: String? = savedStateHandle["bank_id"]
        set(value) {
            savedStateHandle["bank_id"] = bankId
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
            getBanks(query = null)
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_START_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
    }

    override fun updateCollectedData(collectedData: BanksCollectableData) {
        viewModelScope.launch {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
        onCollectableDataUpdated(collectedData)
    }

    override fun submit() {
        viewModelScope.launch {
            runSuspendCatching {
                requireNotNullCheck(value = bankId, key = BanksIllegalValueKey.BANK_ID)
            }.flatMap { bankId ->
                bankIssuerTokenizationDelegate.tokenize(issuerBankId = bankId)
            }
                .onSuccess { onFinished() }
                .onFailure { throwable -> handleError(throwable) }
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_SUBMIT_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
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
            .onFailure { throwable -> handleError(throwable) }
    }

    private suspend fun onCollectFilter(filter: BanksCollectableData.Filter) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = filter)
        )
        if (banks == null) {
            validationErrorLoggingDelegate.logSdkAnalyticsError(banksNotLoadedPrimerValidationError)
            _componentValidationStatus.emit(
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(banksNotLoadedPrimerValidationError),
                    collectableData = filter
                )
            )
        } else {
            _componentValidationStatus.emit(
                PrimerValidationStatus.Valid(collectableData = filter)
            )
            getBanks(query = filter.text)
        }
    }

    private suspend fun onCollectBankId(bankId: BanksCollectableData.BankId) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = bankId)
        )

        val banks = this.banks

        val validationError = BankIdValidator.validate(banks = banks, bankId = bankId.id)

        _componentValidationStatus.emit(
            if (validationError != null) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(validationError),
                    collectableData = bankId
                )
            } else {
                this.bankId = bankId.id
                PrimerValidationStatus.Valid(collectableData = bankId)
            }
        )
    }

    private fun handleError(throwable: Throwable) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
            .also { error ->
                _componentError.emit(error)
                errorLoggingDelegate.logSdkAnalyticsErrors(error = error)
            }
    }
    // endregion

    internal companion object {
        fun provideInstance(
            owner: ViewModelStoreOwner,
            paymentMethodType: String,
            onFinished: () -> Unit,
            onDisposed: () -> Unit
        ) = BanksComponentProvider.provideInstance(
            owner = owner,
            paymentMethodType = paymentMethodType,
            onFinished = onFinished,
            onDisposed = onDisposed
        )
    }
}
