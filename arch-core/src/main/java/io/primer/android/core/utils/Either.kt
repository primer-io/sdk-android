package io.primer.android.core.utils

sealed class Either<A, B>

class Success<A, B>(val value: A) : Either<A, B>()
class Failure<A, B>(val value: B) : Either<A, B>()
