package io.primer.android

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental API. It may be changed or removed in the future."
)
annotation class ExperimentalPrimerApi
