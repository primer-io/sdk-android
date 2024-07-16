package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequiredAction
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayRequiredActionInteractor(
    private val clientTokenRepository: ClientTokenRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseSuspendInteractor<NolPayRequiredAction, None>() {
    override suspend fun performAction(params: None) = runSuspendCatching {
        NolPayRequiredAction(
            requireNotNullCheck(
                clientTokenRepository.getTransactionNo(),
                NolPayIllegalValueKey.TRANSACTION_NUMBER
            ),
            requireNotNullCheck(
                clientTokenRepository.getStatusUrl(),
                NolPayIllegalValueKey.STATUS_URL
            ),
            requireNotNullCheck(
                clientTokenRepository.getRedirectUrl(),
                NolPayIllegalValueKey.COMPLETE_URL
            ),
            PaymentMethodType.NOL_PAY.name
        )
    }.onFailure { throwable ->
        errorEventResolver.resolve(throwable, ErrorMapperType.NOL_PAY)
    }
}
