package io.primer.components.clean.internal.domain.usecases

import io.primer.components.clean.internal.data.mappers.PaymentMethodMapper
import io.primer.components.clean.internal.domain.models.PaymentMethod
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository
import io.primer.components.clean.model.PrimerPaymentMethod

internal class GetAvailablePaymentMethodsUseCase(
    private val headlessRepository: HeadlessRepository,
    private val paymentMethodMapper: PaymentMethodMapper
) {

    suspend operator fun invoke(): Result<List<PrimerPaymentMethod>> {
        headlessRepository.getAvailablePaymentMethods()
        return Result.success(
            emptyList<PaymentMethod>()
                .map { paymentMethodMapper.toPublic(it) })
    }
}
