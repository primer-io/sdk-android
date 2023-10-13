package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class PaymentRawDataTypeValidateInteractor(
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val errorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<Unit, PaymentRawDataParams>() {

    override fun execute(params: PaymentRawDataParams) =
        flowOf(
            paymentMethodMapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(
                params.paymentMethodType
            )
        ).map {
            val requiredInputDataClass = it.requiredInputDataClass?.java
            if (requiredInputDataClass != params.inputData::class.java) {
                throw InvalidTokenizationDataException(
                    params.paymentMethodType,
                    params.inputData::class,
                    requiredInputDataClass?.kotlin
                )
            }

            Unit
        }.doOnError {
            errorEventResolver.resolve(it, ErrorMapperType.HUC)
        }.flowOn(dispatcher)
}
