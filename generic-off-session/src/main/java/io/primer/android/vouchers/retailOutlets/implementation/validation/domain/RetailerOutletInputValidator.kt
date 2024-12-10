package io.primer.android.vouchers.retailOutlets.implementation.validation.domain

import io.primer.android.PrimerRetailerData
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository

internal class RetailerOutletInputValidator(
    private val retailerOutletRepository: RetailOutletRepository
) : PaymentInputDataValidator<PrimerRetailerData> {
    override suspend fun validate(rawData: PrimerRetailerData) =
        listOfNotNull(
            XenditRetailerOutletValidator(retailerOutletRepository)
                .validate(rawData.id)
        )
}
