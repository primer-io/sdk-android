package io.primer.android.internal.domain.usecase

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.NativeUiRepository

internal class StartNativeUiPaymentUseCase(
    private val nativeUiRepository: NativeUiRepository,
    private val headlessRepository: HeadlessRepository,
) : Cleanable {

    suspend operator fun invoke(
        paymentMethodType: String,
    ): Result<PrimerCheckoutData> {
        nativeUiRepository.startPaymentFlow(paymentMethodType)
        return headlessRepository.awaitPaymentResult()
    }

    override fun cleanup() {
        nativeUiRepository.cleanup()
    }
}
