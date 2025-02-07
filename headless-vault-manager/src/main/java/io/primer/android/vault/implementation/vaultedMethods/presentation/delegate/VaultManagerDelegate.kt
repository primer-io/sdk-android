package io.primer.android.vault.implementation.vaultedMethods.presentation.delegate

import androidx.annotation.VisibleForTesting
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.domain.None
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.onError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.vault.implementation.vaultedMethods.domain.FetchVaultedPaymentMethodsInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.FindVaultedPaymentMethodInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultDeleteParams
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultPaymentMethodIdParams
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultTokenParams
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.resolvers.VaultManagerInitValidationRulesResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
internal class VaultManagerDelegate(
    private val initValidationRulesResolver: VaultManagerInitValidationRulesResolver,
    private val vaultedPaymentMethodsInteractor: FetchVaultedPaymentMethodsInteractor,
    private val vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor,
    private val vaultedPaymentMethodsExchangeInteractor: VaultedPaymentMethodsExchangeInteractor,
    private val findVaultedPaymentMethodInteractor: FindVaultedPaymentMethodInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val vaultedPaymentMethodAdditionalDataValidatorRegistry: VaultedPaymentMethodAdditionalDataValidatorRegistry,
) : DISdkComponent {
    private val scope = CoroutineScope(SupervisorJob())

    fun init() {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_INIT))
        val validationResults =
            initValidationRulesResolver.resolve().rules.map { rule ->
                rule.validate(Unit)
            }

        validationResults.filterIsInstance<ValidationResult.Failure>()
            .forEach { validationResult ->
                throw validationResult.exception
            }
    }

    suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethod>> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_FETCH))
        return vaultedPaymentMethodsInteractor(None)
    }

    suspend fun deletePaymentMethod(vaultedPaymentMethodId: String): Result<Unit> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_DELETE))
        return findVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId,
            ),
        ).flatMap { vaultedToken ->
            vaultedPaymentMethodsDeleteInteractor(VaultDeleteParams(vaultedToken.id)).map { }
        }.onError { throwable ->
            logErrorEvent(throwable)
        }
    }

    suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ): Result<List<PrimerValidationError>> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_VALIDATE))
        return findVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId,
            ),
        ).map { vaultedToken ->
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(additionalData)
                .validate(additionalData, vaultedToken)
        }.onError { throwable ->
            logErrorEvent(throwable)
        }
    }

    suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData? = null,
    ): Result<PrimerPaymentMethodTokenData> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_START_PAYMENT_FLOW))
        return findVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId,
            ),
        ).flatMap { vaultedToken ->
            vaultedPaymentMethodsExchangeInteractor(
                with(vaultedToken) { VaultTokenParams(id, paymentMethodType, additionalData) },
            )
        }.onError { throwable ->
            logErrorEvent(throwable)
        }
    }

    private fun logErrorEvent(throwable: Throwable) =
        errorMapperRegistry.getPrimerError(throwable)
            .also { error ->
                addAnalyticsEvent(
                    MessageAnalyticsParams(
                        messageType = MessageType.ERROR,
                        message = error.description,
                        severity = Severity.ERROR,
                        diagnosticsId = error.diagnosticsId,
                        context = error.context,
                    ),
                )
            }

    private fun addAnalyticsEvent(params: BaseAnalyticsParams) =
        scope.launch {
            analyticsInteractor(params)
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal companion object {
        const val ANALYTICS_EVENT_INIT = "HeadlessVaultManager.newInstance()"
        const val ANALYTICS_EVENT_FETCH = "HeadlessVaultManager.fetchVaultedPaymentMethods()"
        const val ANALYTICS_EVENT_DELETE = "HeadlessVaultManager.deletePaymentMethod()"
        const val ANALYTICS_EVENT_START_PAYMENT_FLOW = "HeadlessVaultManager.startPaymentFlow()"
        const val ANALYTICS_EVENT_VALIDATE = "HeadlessVaultManager.validate()"
    }
}
