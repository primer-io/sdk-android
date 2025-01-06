package io.primer.android.sandboxProcessor.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable
import io.primer.android.sandboxProcessor.SandboxProcessorDecisionType

internal data class SandboxProcessorTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
    val decisionType: SandboxProcessorDecisionType,
) : TokenizationInputable
