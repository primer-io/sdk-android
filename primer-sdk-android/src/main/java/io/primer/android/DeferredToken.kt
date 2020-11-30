package io.primer.android

class DeferredToken(provider: IClientTokenProvider) {
  private var value: String? = null
  private var observer: ((String) -> Unit)? = null

  init {
    provider.createToken { token ->
      value = token
      onValue()
    }
  }

  fun observe(observer: ((String) -> Unit)) {
    this.observer = observer
    onValue()
  }

  private fun onValue() {
    if (value != null) {
      this.observer?.invoke(value!!)
    }
  }
}