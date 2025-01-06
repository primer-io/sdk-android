package io.primer.android.sandboxProcessor.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class SandboxProcessorConfig(
    val paymentMethodConfigId: String,
    val locale: String,
) : PaymentMethodConfiguration
