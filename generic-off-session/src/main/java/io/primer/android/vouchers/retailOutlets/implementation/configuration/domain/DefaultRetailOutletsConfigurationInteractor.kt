package io.primer.android.vouchers.retailOutlets.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfig
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfigParams

internal typealias RetailOutletsConfigurationInteractor =
    PaymentMethodConfigurationInteractor<RetailOutletsConfig, RetailOutletsConfigParams>

internal class DefaultRetailOutletsConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<RetailOutletsConfig, RetailOutletsConfigParams>,
) : PaymentMethodConfigurationInteractor<RetailOutletsConfig, RetailOutletsConfigParams>(
        configurationRepository,
    )
