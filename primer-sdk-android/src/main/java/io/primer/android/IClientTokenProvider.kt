package io.primer.android

interface IClientTokenProvider {
  fun createToken(callback: ((String) -> Unit))
}