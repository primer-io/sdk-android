package io.primer.android.paymentmethods.core.composer

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessComponent
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessContextualStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessDataCollectable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessInputValidatable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessMetadatable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStateMetadatable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSubmitable
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.job
import kotlin.properties.Delegates

/**
 * An interface defining the contract for composing and managing payment methods.
 *
 * This interface serves as a blueprint for classes that need to implement
 * the logic for creating and handling payment methods in a standardized way.
 */
fun interface PaymentMethodComposer {

    fun cancel()
}

/**
 * An interface representing a raw data payment method component that combines multiple capabilities for handling
 * payment methods in a headless fashion.
 *
 * @param TCollectableData The type of data that can be collected by this component, extending [PrimerCollectableData].
 */
abstract class RawDataPaymentMethodComponent<TCollectableData : PrimerCollectableData> :
    PrimerHeadlessComponent,
    PrimerHeadlessInputValidatable<TCollectableData>,
    PrimerHeadlessDataCollectable<TCollectableData>,
    PrimerHeadlessStateMetadatable<TCollectableData>,
    PrimerHeadlessMetadatable<TCollectableData>,
    PrimerHeadlessSubmitable,
    PrimerHeadlessContextualStartable,
    PaymentMethodComposer,
    DISdkComponent {

    protected val composerScope by lazy {
        CoroutineScope(
            SupervisorJob(
                parent = resolve<CoroutineScopeProvider>().scope.coroutineContext.job
            ) + Dispatchers.Main
        )
    }

    override fun cancel() {
        composerScope.cancel()
    }
}

interface VaultedPaymentMethodComponent :
    PrimerHeadlessContextualStartable,
    PaymentMethodComposer {

    val paymentDelegate: PaymentMethodPaymentDelegate
}

abstract class InternalNativeUiPaymentMethodComponent :
    PrimerHeadlessComponent,
    UiEventable,
    PaymentMethodComposer,
    DISdkComponent {

    protected val composerScope by lazy {
        CoroutineScope(
            SupervisorJob(
                parent = resolve<CoroutineScopeProvider>().scope.coroutineContext.job
            ) + Dispatchers.Main
        )
    }

    protected var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    protected var paymentMethodType by Delegates.notNull<String>()

    protected open val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    abstract fun start(paymentMethodType: String, primerSessionIntent: PrimerSessionIntent)

    override fun cancel() {
        composerScope.cancel()
    }
}
