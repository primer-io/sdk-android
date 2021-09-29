package io.primer.android.utils

internal sealed class Either<A, B>

internal class Success<A, B>(val value: A) : Either<A, B>()
internal class Failure<A, B>(val value: B) : Either<A, B>()
