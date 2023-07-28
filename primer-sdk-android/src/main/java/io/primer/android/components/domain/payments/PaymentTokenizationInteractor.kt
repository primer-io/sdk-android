package io.primer.android.components.domain.payments

import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

internal class PaymentTokenizationInteractor(
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<BasePaymentInstrumentParams, PaymentTokenizationDescriptorParams>() {

    override fun execute(params: PaymentTokenizationDescriptorParams):
        Flow<BasePaymentInstrumentParams> {
        return paymentMethodDescriptorsRepository.resolvePaymentMethodDescriptors()
            .mapLatest { descriptors ->
                val descriptor = descriptors.first { it.config.type == params.paymentMethodType }
                val rawDataDefinition = descriptor.headlessDefinition?.rawDataDefinition
                if (
                    params.inputData::class.java != rawDataDefinition?.requiredInputDataClass?.java
                ) {
                    throw InvalidTokenizationDataException(
                        params.paymentMethodType,
                        params.inputData::class,
                        rawDataDefinition?.requiredInputDataClass
                    )
                } else {
                    rawDataDefinition.rawDataMapper.getInstrumentParams(params.inputData)
                }
            }.doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.HUC)
            }.flowOn(dispatcher)
    }
}
