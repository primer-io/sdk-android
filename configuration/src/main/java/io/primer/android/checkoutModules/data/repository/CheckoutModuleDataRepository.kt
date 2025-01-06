package io.primer.android.checkoutModules.data.repository

import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.configuration.data.model.CheckoutModuleType
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.toCheckoutModule
import io.primer.android.core.data.datasource.BaseCacheDataSource

internal class CheckoutModuleDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : CheckoutModuleRepository {
    override fun getCardInformation(): CheckoutModule.CardInformation? {
        return getCheckoutModule(CheckoutModuleType.CARD_INFORMATION)?.let {
            it as CheckoutModule.CardInformation
        }
    }

    override fun getBillingAddress(): CheckoutModule.BillingAddress? {
        return getCheckoutModule(CheckoutModuleType.BILLING_ADDRESS)?.let {
            it as CheckoutModule.BillingAddress
        }
    }

    private fun getCheckoutModule(type: CheckoutModuleType): CheckoutModule? =
        configurationDataSource.get().checkoutModules.find {
            it.type == type
        }?.toCheckoutModule()
}
