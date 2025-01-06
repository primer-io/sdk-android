package io.primer.android.clientToken.core.errors.data.exception

class ExpiredClientTokenException(
    override val message: String = "Cannot initialize the SDK because the client token provided is expired.",
) : IllegalArgumentException()
