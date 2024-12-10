package io.primer.android.core.utils

fun interface BaseDataProvider<T> {

    fun provide(): T
}

fun interface BaseDataWithInputProvider<I, O> {

    fun provide(input: I): O
}
