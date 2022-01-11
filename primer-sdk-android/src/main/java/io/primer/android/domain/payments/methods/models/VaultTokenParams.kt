package io.primer.android.domain.payments.methods.models

import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.domain.base.Params

internal data class VaultTokenParams(val token: PaymentMethodVaultTokenInternal) : Params
