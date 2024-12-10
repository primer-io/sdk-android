package io.primer.android.components

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.implementation.core.presentation.PaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.PaymentMethodStarter
import io.primer.android.components.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.components.validation.rules.PaymentMethodManagerSessionIntentValidationData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.payments.core.helpers.PreparationStartHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

internal interface NativeUiManagerHeadlessDelegate {

    fun dispatchAction(
        type: String,
        completion: ((Error?) -> Unit) = {}
    )

    fun cleanup()
}

internal class DefaultNativeUiManagerHeadlessManagerDelegate(
    private val actionInteractor: ActionInteractor,
    private val sessionIntentRulesResolver: PaymentMethodManagerSessionIntentRulesResolver,
    private val paymentMethodInitializer: PaymentMethodInitializer,
    private val paymentMethodStarter: PaymentMethodStarter,
    private val composerRegistry: PaymentMethodComposerRegistry,
    private val preparationStartHandler: PreparationStartHandler,
    private val headlessScopeProvider: CoroutineScopeProvider
) : NativeUiManagerHeadlessDelegate,
    DefaultPaymentMethodManagerDelegate(paymentMethodInitializer, paymentMethodStarter) {

    private val scope = CoroutineScope(headlessScopeProvider.scope.coroutineContext)
    private lateinit var component: InternalNativeUiPaymentMethodComponent

    override fun start(
        context: Context,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        category: PrimerPaymentMethodManagerCategory,
        onPostStart: () -> Unit
    ) {
        val validationResults = sessionIntentRulesResolver.resolve().rules.map {
            it.validate(
                PaymentMethodManagerSessionIntentValidationData(
                    paymentMethodType = paymentMethodType,
                    sessionIntent = sessionIntent
                )
            )
        }

        validationResults.filterIsInstance<ValidationResult.Failure>()
            .forEach { validationResult ->
                throw validationResult.exception
            }
        super.start(context, paymentMethodType, sessionIntent, category, onPostStart = {
            component =
                composerRegistry[paymentMethodType] as InternalNativeUiPaymentMethodComponent
            component.start(paymentMethodType, sessionIntent)
        })
    }

    override fun dispatchAction(type: String, completion: (Error?) -> Unit) {
        scope.launch {
            preparationStartHandler.handle(type)
            actionInteractor(
                MultipleActionUpdateParams(
                    listOf(ActionUpdateSelectPaymentMethodParams(type))
                )
            ).onFailure {
                completion(Error(it))
            }.onSuccess {
                completion(null)
            }
        }
    }

    override fun cleanup() {
        if (::component.isInitialized) component.cancel()
        scope.coroutineContext.cancelChildren()
    }
}
