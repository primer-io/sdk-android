package io.primer.android.assets.ui.registry

import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.model.UnknownBrand

interface BrandRegistry {

    fun register(paymentMethodType: String, brand: Brand)

    fun getBrand(paymentMethodType: String): Brand
}

internal class DefaultBrandRegistry : BrandRegistry {

    private val brands = mutableMapOf<String, Brand>()

    override fun register(paymentMethodType: String, brand: Brand) {
        brands[paymentMethodType] = brand
    }

    override fun getBrand(paymentMethodType: String): Brand {
        return brands[paymentMethodType] ?: UnknownBrand
    }
}
