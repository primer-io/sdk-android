package io.primer.android.model

import io.primer.android.ClientTokenProvider

internal class DeferredToken(
    private val provider: ClientTokenProvider
) {

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
        provider.createToken { token: String ->
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
