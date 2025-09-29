package io.primer.android.internal.domain.usecase

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.NativeUiRepository
import kotlinx.coroutines.flow.first

internal class StartNativeUiPaymentUseCase(
    private val nativeUiRepository: NativeUiRepository,
    private val headlessRepository: HeadlessRepository,
) {

    suspend operator fun invoke(
        paymentMethodType: String,
    ): Result<PrimerCheckoutData> {
        nativeUiRepository.startPaymentFlow(paymentMethodType)
        return headlessRepository.paymentResults.first()
    }

    fun cleanup() {
        nativeUiRepository.cleanup()
    }
}
