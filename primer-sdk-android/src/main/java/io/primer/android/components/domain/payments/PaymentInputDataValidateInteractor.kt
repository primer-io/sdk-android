package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

internal class PaymentInputDataValidateInteractor(
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher
) : BaseInteractor<Unit, PaymentTokenizationDescriptorParams>() {

    override fun execute(params: PaymentTokenizationDescriptorParams) =
        flow {
            val requiredInputDataClass = paymentMethodMapper
                .getPrimerHeadlessUniversalCheckoutPaymentMethod(params.paymentMethodType)
                .requiredInputDataClass?.java
            if (requiredInputDataClass != params.inputData::class.java)
                throw InvalidTokenizationDataException(
                    params.paymentMethodType,
                    params.inputData::class
                )

            emit(Unit)
        }.onStart { eventDispatcher.dispatchEvent(CheckoutEvent.PreparationStarted) }
            .doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.HUC)
            }
}
