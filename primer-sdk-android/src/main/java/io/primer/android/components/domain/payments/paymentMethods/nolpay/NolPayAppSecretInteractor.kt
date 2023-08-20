package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPaySecretParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.domain.base.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayAppSecretInteractor(
    private val secretRepository: NolPayAppSecretRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) : BaseSuspendInteractor<String, NolPaySecretParams>() {
    override suspend fun performAction(params: NolPaySecretParams): Result<String> {
        return secretRepository.getAppSecret(params.sdkId)
    }
}
