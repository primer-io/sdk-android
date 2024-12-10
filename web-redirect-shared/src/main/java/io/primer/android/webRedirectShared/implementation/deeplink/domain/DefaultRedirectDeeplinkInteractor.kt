package io.primer.android.webRedirectShared.implementation.deeplink.domain

import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository

typealias RedirectDeeplinkInteractor = BaseInteractor<String, None>

class DefaultRedirectDeeplinkInteractor(
    private val deeplinkRepository: RedirectDeeplinkRepository
) : BaseInteractor<String, None>() {
    override fun execute(params: None): String {
        return deeplinkRepository.getDeeplinkUrl()
    }
}
