package io.primer.android.paymentMethods.core

import io.primer.android.core.domain.BaseInteractor
import io.primer.android.paymentMethods.core.domain.model.HeadlessSdkInitParams
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository

internal class PrimerHeadlessSdkInitInteractor(private val headlessRepository: PrimerHeadlessRepository) :
    BaseInteractor<Unit, HeadlessSdkInitParams>() {
    override fun execute(params: HeadlessSdkInitParams) {
        headlessRepository.start(params.clientToken)
    }
}
