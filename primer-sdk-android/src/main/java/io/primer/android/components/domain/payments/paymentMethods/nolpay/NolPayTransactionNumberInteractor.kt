package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayTransactionNumberInteractor(
    private val clientTokenRepository: ClientTokenRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseSuspendInteractor<String, None>() {
    override suspend fun performAction(params: None): Result<String> = runSuspendCatching {
        requireNotNullCheck(
            clientTokenRepository.getTransactionNo(),
            NolPayIllegalValueKey.TRANSACTION_NUMBER
        )
    }.onFailure { throwable ->
        errorEventResolver.resolve(throwable, ErrorMapperType.NOL_PAY)
    }
}
