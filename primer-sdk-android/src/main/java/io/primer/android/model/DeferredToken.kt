package io.primer.android.model

import io.primer.android.IClientTokenProvider

internal class DeferredToken(private val provider: IClientTokenProvider) {
  private var value: String? = null
  private var observer: ((String) -> Unit)? = null

  init {
    load()
  }

  fun observe(observer: ((String) -> Unit)) {
    this.observer = observer
    onValue()
  }

  private fun load() {
    provider.createToken { token ->
      value = token
      onValue()
    }
  }

  private fun onValue() {
    if (value != null) {
      this.observer?.invoke(value!!)
    }
  }
}