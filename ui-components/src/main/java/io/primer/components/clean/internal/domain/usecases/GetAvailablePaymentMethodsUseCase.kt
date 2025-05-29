package io.primer.components.clean.internal.domain.usecases

import io.primer.components.clean.internal.data.mappers.PaymentMethodMapper
import io.primer.components.clean.internal.domain.repositories.HeadlessRepository

internal class GetAvailablePaymentMethodsUseCase(
    private val headlessRepository: HeadlessRepository,
    private val paymentMethodMapper: PaymentMethodMapper
) {

    suspend operator fun invoke() = runCatching {
        headlessRepository.getAvailablePaymentMethods()
            .map { paymentMethodMapper.toInternal(it) }
            .map { paymentMethodMapper.toPublic(it) }
    }
}
