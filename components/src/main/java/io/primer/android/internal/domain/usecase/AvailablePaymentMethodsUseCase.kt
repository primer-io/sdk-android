package io.primer.android.internal.domain.usecase

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.domain.None
import io.primer.android.internal.data.mappers.PaymentMethodMapper
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor

internal class AvailablePaymentMethodsUseCase(
    private val headlessRepository: HeadlessRepository,
    private val paymentMethodMapper: PaymentMethodMapper,
    private val surchargeInteractor: SurchargeInteractor,
) : DISdkComponent {

    var cache: List<PrimerComposablePaymentMethod> = emptyList()
        private set

    suspend operator fun invoke() = runCatching {
        val rawPaymentMethods = headlessRepository.getAvailablePaymentMethods()
        val surcharges = try {
            surchargeInteractor.execute(None)
        } catch (_: Exception) {
            emptyMap()
        }

        cache = rawPaymentMethods.map { paymentMethodMapper.toComposable(it, surcharges) }
    }
}
