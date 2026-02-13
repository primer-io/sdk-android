package io.primer.android.payments.core.idempotency

class IdempotencyKeyHolder {
    private var currentKey: String? = null

    fun set(key: String?) {
        currentKey = key
    }

    fun get(): String? = currentKey

    fun clear() {
        currentKey = null
    }
}
