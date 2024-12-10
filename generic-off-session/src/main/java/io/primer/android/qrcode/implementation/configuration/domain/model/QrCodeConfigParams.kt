package io.primer.android.qrcode.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class QrCodeConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams
