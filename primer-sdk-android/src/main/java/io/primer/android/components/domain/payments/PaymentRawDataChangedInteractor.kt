package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetrieverFactory
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidatorFactory
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlin.properties.Delegates

internal class PaymentRawDataChangedInteractor(
    private val validatorsFactory: PaymentInputDataValidatorFactory,
    private val paymentRawDataMetadataRetrieverFactory: PaymentRawDataMetadataRetrieverFactory,
    private val eventDispatcher: EventDispatcher,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<Unit, PaymentTokenizationDescriptorParams>() {

    private var errors by
    Delegates.observable<List<PrimerInputValidationError>?>(null) { _, oldValue, newValue ->
        run {
            if (oldValue != newValue) eventDispatcher.dispatchEvent(
                CheckoutEvent.HucValidationError(newValue.orEmpty())
            )
        }
    }

    private var metadata by
    Delegates.observable<PrimerPaymentMethodMetadata?>(
        null
    ) { _, oldValue, newValue ->
        run {
            if (oldValue != newValue && newValue != null) eventDispatcher.dispatchEvent(
                CheckoutEvent.HucMetadataChanged(newValue)
            )
        }
    }

    override fun execute(params: PaymentTokenizationDescriptorParams) =
        validatorsFactory.getPaymentInputDataValidator(params.paymentMethodType, params.inputData)
            .validate(params.inputData).combine(
                paymentRawDataMetadataRetrieverFactory.getMetadataRetriever(params.inputData)
                    .retrieveMetadata(params.inputData)
            ) { errors, metadata ->
                this.errors = errors
                this.metadata = metadata
            }.flowOn(dispatcher)
}
