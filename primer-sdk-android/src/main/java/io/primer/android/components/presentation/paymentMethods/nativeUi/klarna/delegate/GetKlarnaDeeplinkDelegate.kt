package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.extensions.runSuspendCatching

internal class GetKlarnaDeeplinkDelegate(private val interactor: KlarnaDeeplinkInteractor) {
    suspend fun getDeeplink(): Result<String> =
        runSuspendCatching { interactor.execute(None()) }
}
