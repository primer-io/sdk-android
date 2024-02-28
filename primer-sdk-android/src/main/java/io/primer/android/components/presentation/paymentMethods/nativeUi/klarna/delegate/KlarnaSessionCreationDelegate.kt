package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.domain.base.None
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.last

internal class KlarnaSessionCreationDelegate(private val interactor: KlarnaSessionInteractor) {
    suspend fun createSession(): Result<KlarnaSession> =
        runSuspendCatching { interactor.execute(None()).last() }
}
