package io.primer.android.banks.implementation.errors.data.exception

internal enum class BanksIllegalValueKey(override val key: String) :
    io.primer.android.errors.data.exception.IllegalValueKey {
    BANK_ID("bankId")
}
