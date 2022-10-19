package io.primer.android.components.domain.payments.validation.retailerOutlet

import io.primer.android.components.domain.core.models.retailOutlet.PrimerRawRetailerData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidator
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class XenditRetailerOutletInputValidator(
    private val retailerOutletRepository: RetailOutletRepository
) :
    PaymentInputDataValidator<PrimerRawRetailerData> {
    override fun validate(rawData: PrimerRawRetailerData): Flow<List<PrimerInputValidationError>?> {
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
