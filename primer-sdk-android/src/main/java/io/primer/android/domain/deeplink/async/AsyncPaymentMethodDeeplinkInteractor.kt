package io.primer.android.domain.deeplink.async

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository

internal class AsyncPaymentMethodDeeplinkInteractor(
    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository
) :
    BaseFlowInteractor<String, None>() {
    override fun execute(params: None) = deeplinkRepository.getDeeplinkUrl()
}
