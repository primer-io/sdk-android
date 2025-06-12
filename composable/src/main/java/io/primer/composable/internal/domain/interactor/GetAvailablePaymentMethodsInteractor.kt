package io.primer.composable.internal.domain.interactor

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.domain.None
import io.primer.composable.internal.data.mappers.PaymentMethodMapper
import io.primer.composable.internal.domain.repositories.HeadlessRepository
import io.primer.ui.core.payment.domain.interactor.SurchargeInteractor

internal class GetAvailablePaymentMethodsInteractor : DISdkComponent {

    private val headlessRepository: HeadlessRepository by lazy { resolve() }
    private val paymentMethodMapper: PaymentMethodMapper by lazy { resolve() }
    private val surchargeInteractor: SurchargeInteractor by lazy { resolve() }

    suspend operator fun invoke() = runCatching {
        val paymentMethods = headlessRepository.getAvailablePaymentMethods()
        val surcharges = try {
            surchargeInteractor.execute(None)
        } catch (e: Exception) {
            emptyMap()
        }
        
        paymentMethods.map { paymentMethodMapper.toComposable(it, surcharges) }
    }
}
