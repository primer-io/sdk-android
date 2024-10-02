package io.primer.android.components.domain.payments.repository

import io.primer.android.domain.session.models.CheckoutModule

internal interface CheckoutModuleRepository {

    fun getCardInformation(): CheckoutModule.CardInformation?

    fun getBillingAddress(): CheckoutModule.BillingAddress?
}
