package io.primer.executionengine.data.executors.http

/**
 * Thrown when an in-flight `http.request` step is aborted by [HttpRequestStepExecutor.onFinish]
 * (as opposed to structured cancellation of the flow itself, which is rethrown).
 */
internal class HttpStepAbortedException(
    cause: Throwable? = null,
) : RuntimeException("http.request aborted", cause)
