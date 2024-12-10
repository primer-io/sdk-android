package io.primer.android.sandboxProcessor.implementation.components

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.extensions.flatMap
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessDataCollectable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable
import io.primer.android.sandboxProcessor.SandboxProcessorDecisionType
import io.primer.android.sandboxProcessor.implementation.payment.delegate.SandboxProcessorPaymentDelegate
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.SandboxProcessorTokenizationDelegate
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.composable.SandboxProcessorTokenizationInputable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProcessorTestComponent internal constructor(
    private val tokenizationDelegate: SandboxProcessorTokenizationDelegate,
    private val paymentDelegate: SandboxProcessorPaymentDelegate
) : InternalNativeUiPaymentMethodComponent(),
    PrimerHeadlessDataCollectable<ProcessorTestCollectableData>,
    PrimerHeadlessSteppable<ProcessorTestStep> {
    private var decisionType by Delegates.notNull<SandboxProcessorDecisionType>()

    private val _componentStep = MutableSharedFlow<ProcessorTestStep>(extraBufferCapacity = 1)
    override val componentStep: Flow<ProcessorTestStep> = _componentStep

    override fun updateCollectedData(collectedData: ProcessorTestCollectableData) {
        decisionType = collectedData.decisionType
    }

    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent
    ) {
        _componentStep.tryEmit(ProcessorTestStep.Started)
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent
        tokenize()
    }

    private fun tokenize() = composerScope.launch {
        runCatching {
            decisionType
        }
            .flatMap {
                tokenizationDelegate.tokenize(
                    SandboxProcessorTokenizationInputable(
                        paymentMethodType = paymentMethodType,
                        primerSessionIntent = primerSessionIntent,
                        decisionType = it
                    )
                )
            }
            .onSuccess {
                _componentStep.emit(ProcessorTestStep.Tokenized)
            }
            .flatMap { paymentMethodTokenData ->
                paymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = primerSessionIntent
                )
                    .onSuccess {
                        _componentStep.emit(ProcessorTestStep.Finished)
                    }
            }.onFailure { throwable ->
                paymentDelegate.handleError(throwable)
            }
    }

    companion object : DISdkComponent {
        fun provideInstance(): ProcessorTestComponent = ProcessorTestComponent(
            tokenizationDelegate = resolve(),
            paymentDelegate = resolve()
        )
    }
}

sealed interface ProcessorTestStep : PrimerHeadlessStep {
    data object Started : ProcessorTestStep
    data object Tokenized : ProcessorTestStep
    data object Finished : ProcessorTestStep
}

data class ProcessorTestCollectableData(val decisionType: SandboxProcessorDecisionType) : PrimerCollectableData
