package io.primer.android.components.data.payments.repository

import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.domain.session.models.CheckoutModule
import io.primer.android.domain.session.models.toCheckoutModule

internal class CheckoutModuleDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : CheckoutModuleRepository {

    private fun getCheckoutModule(type: CheckoutModuleType): CheckoutModule? =
        localConfigurationDataSource.getConfiguration().checkoutModules.find {
            it.type == type
        }?.toCheckoutModule()

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
}
