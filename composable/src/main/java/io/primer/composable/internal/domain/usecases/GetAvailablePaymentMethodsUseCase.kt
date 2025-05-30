package io.primer.composable.internal.domain.usecases

import io.primer.composable.internal.data.mappers.PaymentMethodMapper
import io.primer.composable.internal.domain.repositories.HeadlessRepository

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
