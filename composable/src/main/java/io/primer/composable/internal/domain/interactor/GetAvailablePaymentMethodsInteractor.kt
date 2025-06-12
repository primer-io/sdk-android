package io.primer.composable.internal.domain.interactor

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.data.mappers.PaymentMethodMapper
import io.primer.composable.internal.domain.repositories.HeadlessRepository

internal class GetAvailablePaymentMethodsInteractor : DISdkComponent {

    private val headlessRepository: HeadlessRepository by lazy { resolve() }
    private val paymentMethodMapper: PaymentMethodMapper by lazy { resolve() }

    suspend operator fun invoke() = runCatching {
        headlessRepository.getAvailablePaymentMethods()
            .map { paymentMethodMapper.toComposable(it) }
    }
}
