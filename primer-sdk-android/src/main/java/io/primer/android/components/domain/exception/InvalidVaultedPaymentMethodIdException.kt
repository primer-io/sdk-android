package io.primer.android.components.domain.exception

class InvalidVaultedPaymentMethodIdException :
    IllegalArgumentException("The id provided does not match any vaulted payment method")
