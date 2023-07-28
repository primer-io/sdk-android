package io.primer.android.components.presentation.vault

import androidx.annotation.VisibleForTesting
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.models.VaultPaymentMethodIdParams
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsExchangeInteractor
import io.primer.android.components.domain.payments.vault.HeadlessVaultedPaymentMethodsInteractor
import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.components.domain.payments.vault.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.components.domain.payments.vault.validation.resolvers.VaultManagerInitValidationRulesResolver
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.extensions.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
internal class VaultManagerDelegate(
    private val initValidationRulesResolver: VaultManagerInitValidationRulesResolver,
    private val vaultedPaymentMethodsInteractor: HeadlessVaultedPaymentMethodsInteractor,
    private val vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor,
    private val vaultedPaymentMethodsExchangeInteractor:
        HeadlessVaultedPaymentMethodsExchangeInteractor,
    private val headlessVaultedPaymentMethodInteractor: HeadlessVaultedPaymentMethodInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val vaultedPaymentMethodAdditionalDataValidatorRegistry:
        VaultedPaymentMethodAdditionalDataValidatorRegistry,
    private val navigator: Navigator
) : EventBus.EventListener {

    private val scope = CoroutineScope(SupervisorJob())
    private var subscription: EventBus.SubscriptionHandle? = null
    private var transactionId: String? = null

    init {
        subscription = EventBus.subscribe(this)
    }

    fun init() {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_INIT))
        val validationResults = initValidationRulesResolver.resolve().rules.map { rule ->
            rule.validate(Unit)
        }

        validationResults.filterIsInstance<ValidationResult.Failure>()
            .forEach { validationResult ->
                throw validationResult.exception
            }
    }

    suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethod>> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_FETCH))
        return vaultedPaymentMethodsInteractor(None())
    }

    suspend fun deletePaymentMethod(vaultedPaymentMethodId: String): Result<Unit> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_DELETE))
        return headlessVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId
            )
        ).flatMap { vaultedToken ->
            vaultedPaymentMethodsDeleteInteractor(VaultDeleteParams(vaultedToken.id)).map { }
        }
    }

    suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData
    ): Result<List<PrimerValidationError>> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_VALIDATE))
        return headlessVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId
            )
        ).map { vaultedToken ->
            vaultedPaymentMethodAdditionalDataValidatorRegistry.getValidator(additionalData)
                .validate(additionalData, vaultedToken)
        }
    }

    suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData? = null
    ): Result<Unit> {
        addAnalyticsEvent(SdkFunctionParams(ANALYTICS_EVENT_START_PAYMENT_FLOW))
        return headlessVaultedPaymentMethodInteractor(
            VaultPaymentMethodIdParams(
                vaultedPaymentMethodId
            )
        ).map { vaultedToken ->
            vaultedPaymentMethodsExchangeInteractor(
                with(vaultedToken) { VaultTokenParams(id, paymentMethodType, additionalData) }
            ).collect()
        }
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.PaymentContinueVaultHUC -> createPayment(
                e.data.token, e.resumeHandler
            )
            is CheckoutEvent.ResumeSuccessInternalVaultHUC -> resumePayment(
                e.resumeToken, e.resumeHandler
            )
            is CheckoutEvent.Start3DSVault -> {
                if (e.processor3DSData == null) navigator.openThreeDsScreen()
                else navigator.openProcessor3dsViewScreen(
                    e.processor3DSData.title,
                    e.processor3DSData.paymentMethodType,
                    e.processor3DSData.redirectUrl,
                    e.processor3DSData.statusUrl
                )
            }
            else -> Unit
        }
    }

    private fun createPayment(
        paymentMethodToken: String,
        resumeHandler: PrimerResumeDecisionHandler
    ) {
        scope.launch {
            createPaymentInteractor(
                CreatePaymentParams(
                    paymentMethodToken,
                    resumeHandler
                )
            ).collect {
                transactionId = it
            }
        }
    }

    fun resumePayment(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler) {
        scope.launch {
            resumePaymentInteractor(
                ResumeParams(
                    transactionId.orEmpty(),
                    resumeToken,
                    resumeHandler
                )
            ).collect { }
        }
    }

    fun reset() {
        scope.coroutineContext.cancelChildren()
        subscription?.unregister()
        subscription = null
    }

    private fun addAnalyticsEvent(params: BaseAnalyticsParams) = scope.launch {
        analyticsInteractor(params).collect()
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
