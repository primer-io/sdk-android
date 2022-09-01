package io.primer.android.domain.deeplink.klarna

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository

internal class KlarnaDeeplinkInteractor(
    private val deeplinkRepository: KlarnaDeeplinkRepository
) : BaseInteractor<String, None>() {
    override fun execute(params: None) = deeplinkRepository.getDeeplinkUrl()
}
