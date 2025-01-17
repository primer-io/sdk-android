package io.primer.android.paymentMethods.core

import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository

internal class PrimerHeadlessSdkCleanupInteractor(private val headlessRepository: PrimerHeadlessRepository) :
    BaseInteractor<Unit, None>() {
    override fun execute(params: None) {
        headlessRepository.cleanup()
    }
}
