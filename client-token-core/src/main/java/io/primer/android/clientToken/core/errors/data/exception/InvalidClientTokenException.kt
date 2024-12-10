package io.primer.android.clientToken.core.errors.data.exception

class InvalidClientTokenException(
    override val message: String = "The client token provided is not a valid client token"
) : IllegalArgumentException()
