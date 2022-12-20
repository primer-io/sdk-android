package io.primer.android.domain.payments.async.exception

import kotlinx.coroutines.CancellationException

internal class AsyncFlowIgnoredCancellationException : CancellationException()
