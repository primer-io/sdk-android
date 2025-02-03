package io.primer.android.errors.domain

import io.primer.android.core.logging.internal.LogReporter

internal class DefaultErrorResolver(
    errorMapperRegistry: ErrorMapperRegistry,
    logReporter: LogReporter,
) : BaseErrorResolver(errorMapperRegistry = errorMapperRegistry, logReporter = logReporter)
