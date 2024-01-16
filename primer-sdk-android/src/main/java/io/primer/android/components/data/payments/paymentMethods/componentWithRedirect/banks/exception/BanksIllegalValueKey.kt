package io.primer.android.components.data.payments.paymentMethods.componentWithRedirect.banks.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class BanksIllegalValueKey(override val key: String) : IllegalValueKey {
    BANK_ID("bankId")
}
