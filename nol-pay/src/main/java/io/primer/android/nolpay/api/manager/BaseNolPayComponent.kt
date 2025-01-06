package io.primer.android.nolpay.api.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.debounce
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.nolpay.implementation.validation.NolPayValidatorRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class BaseNolPayComponent<C : NolPayCollectableData, S : PrimerHeadlessStep> internal constructor(
    private val validatorRegistry: NolPayValidatorRegistry,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    private val errorMapperRegistry: ErrorMapperRegistry,
) : ViewModel(),
    PrimerHeadlessStartable,
    PrimerHeadlessCollectDataComponent<C>,
    PrimerHeadlessSteppable<S> {
    @Suppress("ktlint:standard:property-naming")
    protected val _componentStep: MutableSharedFlow<S> = MutableSharedFlow()
    override val componentStep: Flow<S> = _componentStep

    private val _componentError: MutableSharedFlow<PrimerError> = MutableSharedFlow()
    override val componentError: Flow<PrimerError> = _componentError

    private val _componentValidationStatus:
        MutableSharedFlow<PrimerValidationStatus<C>> = MutableSharedFlow()
    override val componentValidationStatus:
        Flow<PrimerValidationStatus<C>> = _componentValidationStatus

    protected fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf(),
    ) = viewModelScope.launch {
        eventLoggingDelegate.logSdkAnalyticsEvent(
            PaymentMethodType.NOL_PAY.name,
            methodName,
            context,
        )
    }

    protected fun handleError(throwable: Throwable) =
        viewModelScope.launch {
            errorMapperRegistry.getPrimerError(throwable)
                .also { error ->
                    _componentError.emit(error)
                }.also { error ->
                    errorLoggingDelegate.logSdkAnalyticsErrors(error)
                }
        }

    protected val onCollectableDataUpdated: (C) -> Unit =
        viewModelScope.debounce { collectedData ->
            _componentValidationStatus.emit(PrimerValidationStatus.Validating(collectedData))
            val validationResult =
                validatorRegistry.getValidator(collectedData).validate(
                    collectedData,
                )
            validationResult.onSuccess { errors ->
                _componentValidationStatus.emit(
                    when (errors.isEmpty()) {
                        true -> PrimerValidationStatus.Valid(collectedData)
                        false -> {
                            errors.forEach {
                                validationErrorLoggingDelegate.logSdkAnalyticsError(it)
                            }
                            PrimerValidationStatus.Invalid(errors, collectedData)
                        }
                    },
                )
            }.onFailure { throwable ->
                val error = errorMapperRegistry.getPrimerError(throwable)
                validationErrorLoggingDelegate.logSdkAnalyticsError(error)
                _componentValidationStatus.emit(
                    PrimerValidationStatus.Error(error = error, collectableData = collectedData),
                )
            }
        }
}
