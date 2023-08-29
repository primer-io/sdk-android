package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.base.BaseErrorFlowResolver
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.MutableSharedFlow

internal class NolPayErrorFlowResolver(
    analyticsRepository: AnalyticsRepository,
    errorMapperFactory: ErrorMapperFactory,
) : BaseErrorFlowResolver(ErrorMapperType.NOL_PAY, errorMapperFactory, analyticsRepository) {

    override suspend fun dispatch(error: PrimerError, errorFlow: MutableSharedFlow<PrimerError>) {
        errorFlow.emit(error)
    }
}
