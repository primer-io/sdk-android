package io.primer.android.http.exception

internal class InvalidUrlException(val url: String) : IllegalArgumentException("Provided url: $url is not valid.")
