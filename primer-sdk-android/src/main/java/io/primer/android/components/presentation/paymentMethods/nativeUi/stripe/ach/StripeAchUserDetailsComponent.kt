package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeAchUserDetailsIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.EmailAddressValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.FirstNameValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.LastNameValidator
import io.primer.android.components.manager.ach.PrimerHeadlessAchComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.analytics.StripeAchUserDetailsAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsStep
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetClientSessionCustomerDetailsDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchClientSessionPatchDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.di.StripeAchUserDetailsComponentProvider
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.flatMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class StripeAchUserDetailsComponent internal constructor(
    private val getClientSessionCustomerDetailsDelegate: GetClientSessionCustomerDetailsDelegate,
    private val stripeAchClientSessionPatchDelegate: StripeAchClientSessionPatchDelegate,
    private val stripeAchTokenizationDelegate: StripeAchTokenizationDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val errorEventResolver: BaseErrorEventResolver,
    private val primerSettings: PrimerSettings,
    private val savedStateHandle: SavedStateHandle,
    private val errorMapper: ErrorMapper
) : ViewModel(),
    PrimerHeadlessAchComponent<AchUserDetailsCollectableData, AchUserDetailsStep> {
    private var firstName: String = savedStateHandle.get<String>(FIRST_NAME_KEY).orEmpty()
        set(value) {
            savedStateHandle[FIRST_NAME_KEY] = value
            field = value
        }
    private var lastName: String = savedStateHandle.get<String>(LAST_NAME_KEY).orEmpty()
        set(value) {
            savedStateHandle[LAST_NAME_KEY] = value
            field = value
        }
    private var emailAddress: String = savedStateHandle.get<String>(EMAIL_ADDRESS_KEY).orEmpty()
        set(value) {
            savedStateHandle[EMAIL_ADDRESS_KEY] = value
            field = value
        }
    private var hadFirstSubmission: Boolean = savedStateHandle[HAD_FIRST_SUBMISSION_KEY] ?: false
        set(value) {
            savedStateHandle[HAD_FIRST_SUBMISSION_KEY] = value
            field = value
        }
    private val _componentError = MutableSharedFlow<PrimerError>()
    override val componentError: Flow<PrimerError> = _componentError

    private val _componentValidationStatus =
        MutableSharedFlow<PrimerValidationStatus<AchUserDetailsCollectableData>>()
    override val componentValidationStatus:
        Flow<PrimerValidationStatus<AchUserDetailsCollectableData>> =
            _componentValidationStatus

    private val _componentStep = MutableSharedFlow<AchUserDetailsStep>()
    override val componentStep: Flow<AchUserDetailsStep> = _componentStep

    override fun start() {
        viewModelScope.launch {
            getClientSessionCustomerDetailsDelegate()
                .onSuccess {
                    firstName = it.firstName
                    lastName = it.lastName
                    emailAddress = it.emailAddress
                    _componentStep.emit(
                        AchUserDetailsStep.UserDetailsRetrieved(
                            firstName = it.firstName,
                            lastName = it.lastName,
                            emailAddress = it.emailAddress
                        )
                    )
                }
                .onFailure(::handleError)

            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
    }

    override fun updateCollectedData(collectedData: AchUserDetailsCollectableData) {
        viewModelScope.launch { updateCollectedDataImpl(collectedData) }
    }

    private suspend fun updateCollectedDataImpl(collectedData: AchUserDetailsCollectableData): Boolean {
        _componentValidationStatus.emit(PrimerValidationStatus.Validating(collectedData))

        val validationError = when (collectedData) {
            is AchUserDetailsCollectableData.FirstName -> {
                val error = FirstNameValidator.validate(collectedData.value)
                if (error == null) firstName = collectedData.value
                error
            }

            is AchUserDetailsCollectableData.LastName -> {
                val error = LastNameValidator.validate(collectedData.value)
                if (error == null) lastName = collectedData.value
                error
            }

            is AchUserDetailsCollectableData.EmailAddress -> {
                val error = EmailAddressValidator.validate(collectedData.value)
                if (error == null) emailAddress = collectedData.value
                error
            }
        }

        _componentValidationStatus.emit(
            if (validationError == null) {
                PrimerValidationStatus.Valid(collectedData)
            } else {
                PrimerValidationStatus.Invalid(validationError, collectedData)
            }
        )

        eventLoggingDelegate.logSdkAnalyticsEvent(
            methodName =
            StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
            paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        )
        return validationError == null
    }

    override fun submit() {
        viewModelScope.launch {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )

            if (!hadFirstSubmission) {
                // First time submit is called, trigger validation
                var isValid = updateCollectedDataImpl(AchUserDetailsCollectableData.FirstName(firstName))
                isValid = isValid and updateCollectedDataImpl(AchUserDetailsCollectableData.LastName(lastName))
                isValid = isValid and updateCollectedDataImpl(AchUserDetailsCollectableData.EmailAddress(emailAddress))
                hadFirstSubmission = true

                if (!isValid) {
                    return@launch
                }
            }

            runCatching {
                object {
                    val firstName = requireNotNullCheck(
                        value = this@StripeAchUserDetailsComponent.firstName.takeIf { it.isNotBlank() },
                        key = StripeAchUserDetailsIllegalValueKey.MISSING_FIRST_NAME
                    )
                    val lastName = requireNotNullCheck(
                        value = this@StripeAchUserDetailsComponent.lastName.takeIf { it.isNotBlank() },
                        key = StripeAchUserDetailsIllegalValueKey.MISSING_LAST_NAME
                    )
                    val emailAddress = requireNotNullCheck(
                        value = this@StripeAchUserDetailsComponent.emailAddress.takeIf { it.isNotBlank() },
                        key = StripeAchUserDetailsIllegalValueKey.MISSING_EMAIL_ADDRESS
                    )
                }
            }
                .flatMap {
                    stripeAchClientSessionPatchDelegate(
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailAddress = it.emailAddress
                    )
                }
                .onSuccess {
                    _componentStep.emit(AchUserDetailsStep.UserDetailsCollected)
                }
                .flatMap {
                    stripeAchTokenizationDelegate()
                        .recoverCatching { throw TokenizationFailureException(it) }
                }
                .onFailure(::handleError)
        }
    }

    private fun handleError(throwable: Throwable) = viewModelScope.launch {
        val isTokenizationFailureException = throwable is TokenizationFailureException
        /*
        exclude TokenizationFailureException when in Drop-in because the inner exception is being dispatched by
        TokenizationInteractor.
        */
        if (primerSettings.sdkIntegrationType == SdkIntegrationType.DROP_IN && !isTokenizationFailureException) {
            errorEventResolver.resolve(throwable, ErrorMapperType.STRIPE)
        }
        errorMapper.getPrimerError(if (isTokenizationFailureException) requireNotNull(throwable.cause) else throwable)
            .also { error ->
                _componentError.emit(error)
                errorLoggingDelegate.logSdkAnalyticsErrors(error = error)
            }
    }

    private class TokenizationFailureException(cause: Throwable) : Throwable(cause = cause)

    internal companion object {
        private const val FIRST_NAME_KEY = "first_name"
        private const val LAST_NAME_KEY = "last_name"
        private const val EMAIL_ADDRESS_KEY = "email_address"
        private const val HAD_FIRST_SUBMISSION_KEY = "had_first_submission"

        fun provideInstance(
            owner: ViewModelStoreOwner
        ) = StripeAchUserDetailsComponentProvider.provideInstance(owner)
    }
}
