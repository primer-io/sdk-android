package io.primer.android.domain.base

internal abstract class BaseInteractor<out T, in P : Params> where T : Any {

    abstract fun execute(params: P): T

    operator fun invoke(
        params: P
    ) = execute(params)
}
