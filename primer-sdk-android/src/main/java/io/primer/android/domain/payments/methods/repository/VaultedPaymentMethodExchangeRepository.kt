package io.primer.android.domain.payments.methods.repository

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal fun interface VaultedPaymentMethodExchangeRepository {

    fun exchangeVaultedPaymentToken(
        id: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?
    ): Flow<PaymentMethodTokenInternal>
}
