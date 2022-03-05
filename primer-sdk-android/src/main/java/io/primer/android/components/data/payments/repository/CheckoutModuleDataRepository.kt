package io.primer.android.components.data.payments.repository

import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.model.CheckoutModuleType

internal class CheckoutModuleDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : CheckoutModuleRepository {

    override fun getCheckoutModuleOptions(type: CheckoutModuleType): Map<String, Boolean>? =
        localConfigurationDataSource.getConfiguration().checkoutModules.find {
            it.type == type
        }?.options
}
