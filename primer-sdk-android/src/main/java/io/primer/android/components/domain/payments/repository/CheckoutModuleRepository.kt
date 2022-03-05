package io.primer.android.components.domain.payments.repository

import io.primer.android.data.configuration.model.CheckoutModuleType

internal interface CheckoutModuleRepository {

    fun getCheckoutModuleOptions(type: CheckoutModuleType): Map<String, Boolean>?
}
