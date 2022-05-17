package io.primer.android.components.domain.payments.repository

import io.primer.android.data.configuration.models.CheckoutModuleType

internal interface CheckoutModuleRepository {

    fun getCheckoutModuleOptions(type: CheckoutModuleType): Map<String, Boolean>?
}
