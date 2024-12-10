package io.primer.android.qrcode.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfig
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfigParams

internal typealias QrCodeConfigurationInteractor =
    PaymentMethodConfigurationInteractor<QrCodeConfig, QrCodeConfigParams>

internal class DefaultQrCodeConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<QrCodeConfig, QrCodeConfigParams>
) : PaymentMethodConfigurationInteractor<QrCodeConfig, QrCodeConfigParams>(
    configurationRepository
)
