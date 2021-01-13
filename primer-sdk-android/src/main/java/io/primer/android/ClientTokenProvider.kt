package io.primer.android

interface ClientTokenProvider {
  fun createToken(callback: ((String) -> Unit))
}