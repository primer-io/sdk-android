package io.primer.android.sandboxProcessor.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class SandboxProcessorConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams
