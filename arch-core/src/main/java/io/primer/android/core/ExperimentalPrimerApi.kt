package io.primer.android.core

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message =
        "This is a beta API and can be changed in a " +
            "backwards-incompatible manner with a best-effort migration.",
)
annotation class ExperimentalPrimerApi
