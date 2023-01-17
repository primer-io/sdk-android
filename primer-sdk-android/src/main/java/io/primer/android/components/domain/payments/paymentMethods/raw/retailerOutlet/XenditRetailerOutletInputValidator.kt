package io.primer.android.components.domain.payments.paymentMethods.raw.retailerOutlet

import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class XenditRetailerOutletInputValidator(
    private val retailerOutletRepository: RetailOutletRepository
) : PaymentInputDataValidator<PrimerRetailerData> {
    override fun validate(rawData: PrimerRetailerData): Flow<List<PrimerInputValidationError>> {
        return flow {
            emit(
                listOfNotNull(
                    XenditRetailerOutletValidator(retailerOutletRepository)
                        .validate(rawData.id)
                )
            )
        }
    }
}
