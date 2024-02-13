package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.retailerOutlet

import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository

internal class XenditRetailerOutletInputValidator(
    private val retailerOutletRepository: RetailOutletRepository
) : PaymentInputDataValidator<PrimerRetailerData> {
    override suspend fun validate(rawData: PrimerRetailerData) =
        listOfNotNull(
            XenditRetailerOutletValidator(retailerOutletRepository)
                .validate(rawData.id)
        )
}
