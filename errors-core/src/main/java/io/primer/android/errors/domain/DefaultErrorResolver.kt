package io.primer.android.errors.domain

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter

internal class DefaultErrorResolver(
    analyticsRepository: AnalyticsRepository,
    errorMapperRegistry: ErrorMapperRegistry,
    logReporter: LogReporter,
) : BaseErrorResolver(errorMapperRegistry, analyticsRepository, logReporter)
