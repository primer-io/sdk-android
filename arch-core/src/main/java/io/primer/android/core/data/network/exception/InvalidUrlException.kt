package io.primer.android.core.data.network.exception

class InvalidUrlException(val url: String) : IllegalArgumentException("Provided url: $url is not valid.")
