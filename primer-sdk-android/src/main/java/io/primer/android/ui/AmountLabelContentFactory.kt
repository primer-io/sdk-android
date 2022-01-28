package io.primer.android.ui

import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.model.dto.PrimerConfig

internal class AmountLabelContentFactory private constructor() {

    companion object {

        fun build(config: PrimerConfig, surchargeAmount: Int = 0): MonetaryAmount? {
            val currency = config.monetaryAmount?.currency ?: return null
            val totalAmount = config.monetaryAmount?.value ?: return null
            val amount = totalAmount - surchargeAmount
            return MonetaryAmount.create(currency, amount)
        }
    }
}
