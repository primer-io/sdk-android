package io.primer.android.checkoutModules.domain.repository

import io.primer.android.configuration.domain.model.CheckoutModule

interface CheckoutModuleRepository {
    fun getCardInformation(): CheckoutModule.CardInformation?

    fun getBillingAddress(): CheckoutModule.BillingAddress?
}
