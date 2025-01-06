package io.primer.android.components

import android.content.Context
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.implementation.core.presentation.PaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.PaymentMethodStarter
import io.primer.android.components.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.payments.core.helpers.PreparationStartHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultNativeUiManagerHeadlessManagerDelegateTest {
    private lateinit var actionInteractor: ActionInteractor
    private lateinit var sessionIntentRulesResolver: PaymentMethodManagerSessionIntentRulesResolver
    private lateinit var paymentMethodInitializer: PaymentMethodInitializer
    private lateinit var paymentMethodStarter: PaymentMethodStarter
    private lateinit var composerRegistry: PaymentMethodComposerRegistry
    private lateinit var preparationStartHandler: PreparationStartHandler
    private lateinit var scopeProvider: CoroutineScopeProvider
    private lateinit var nativeUiManagerHeadlessDelegate: DefaultNativeUiManagerHeadlessManagerDelegate

    @BeforeEach
    fun setUp() {
        actionInteractor = mockk(relaxed = true)
        sessionIntentRulesResolver = mockk(relaxed = true)
        paymentMethodInitializer = mockk(relaxed = true)
        paymentMethodStarter = mockk(relaxed = true)
        composerRegistry = mockk(relaxed = true)
        preparationStartHandler = mockk()
        scopeProvider = mockk(relaxed = true)

        every { scopeProvider.scope } returns CoroutineScope(SupervisorJob())

        nativeUiManagerHeadlessDelegate =
            spyk(
                DefaultNativeUiManagerHeadlessManagerDelegate(
                    actionInteractor = actionInteractor,
                    sessionIntentRulesResolver = sessionIntentRulesResolver,
                    paymentMethodInitializer = paymentMethodInitializer,
                    paymentMethodStarter = paymentMethodStarter,
                    composerRegistry = composerRegistry,
                    preparationStartHandler = preparationStartHandler,
                    headlessScopeProvider = scopeProvider,
                ),
            )
    }

    @Disabled("Not sure how to address the onPostStart parameter")
    @Test
    fun `start should initialize and start payment method component`() {
        val context = mockk<Context>(relaxed = true)
        val paymentMethodType = "sampleType"
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val category = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT
        val nativeUiPaymentMethodComponent = mockk<InternalNativeUiPaymentMethodComponent>(relaxed = true)

        every { composerRegistry[paymentMethodType] } returns nativeUiPaymentMethodComponent

        nativeUiManagerHeadlessDelegate.start(context, paymentMethodType, sessionIntent, category)

        coVerify {
            nativeUiPaymentMethodComponent.start(paymentMethodType, sessionIntent)
        }
    }

    @Test
    fun `dispatchAction should call actionInteractor and complete successfully`() {
        coEvery { preparationStartHandler.handle(any()) } just Runs
        val actionType = "sampleAction"
        val completion: (Error?) -> Unit = mockk(relaxed = true)
        val params = MultipleActionUpdateParams(params = listOf(ActionUpdateSelectPaymentMethodParams(actionType)))
        val clientSessionData = mockk<ClientSessionData>(relaxed = true)

        coEvery { actionInteractor(params) } returns Result.success(clientSessionData)

        nativeUiManagerHeadlessDelegate.dispatchAction(actionType, completion)

        coVerifySequence {
            preparationStartHandler.handle(actionType)
            actionInteractor(params)
            completion(null)
        }
    }

    @Test
    fun `dispatchAction should call actionInteractor and complete with error`() {
        coEvery { preparationStartHandler.handle(any()) } just Runs
        val actionType = "sampleAction"
        val completion: (Error?) -> Unit = mockk(relaxed = true)
        val params = MultipleActionUpdateParams(params = listOf(ActionUpdateSelectPaymentMethodParams(actionType)))
        val error = Exception("Sample error")

        coEvery { actionInteractor(params) } returns Result.failure(error)

        every { scopeProvider.scope } returns TestScope()

        nativeUiManagerHeadlessDelegate.dispatchAction(actionType, completion)

        coVerifySequence {
            preparationStartHandler.handle(actionType)
            actionInteractor(params)
            completion(any<Error>())
        }
    }
}
