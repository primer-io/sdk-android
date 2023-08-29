package io.primer.android.domain.base

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.MutableSharedFlow

internal abstract class BaseErrorFlowResolver(
    private val type: ErrorMapperType,
    private val errorMapperFactory: ErrorMapperFactory,
    private val analyticsRepository: AnalyticsRepository
) {
    protected abstract suspend fun dispatch(error: PrimerError, errorFlow: MutableSharedFlow<PrimerError>)

    suspend fun resolve(
        throwable: Throwable,
        errorFlow: MutableSharedFlow<PrimerError>
    ) {
        val error = errorMapperFactory.buildErrorMapper(type).getPrimerError(throwable)
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.ERROR,
                error.description,
                Severity.ERROR,
                error.diagnosticsId,
                error.context
            )
        )
        dispatch(error.exposedError, errorFlow)
    }
}
