package io.primer.android.components.domain.core.mapper

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal fun interface PrimerPaymentMethodRawDataMapper<T : PrimerRawData> {

    fun getInstrumentParams(
        rawData: T
    ): BasePaymentInstrumentParams
}
