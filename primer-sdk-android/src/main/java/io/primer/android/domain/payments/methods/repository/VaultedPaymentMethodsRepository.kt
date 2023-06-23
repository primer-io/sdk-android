package io.primer.android.domain.payments.methods.repository

import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal

internal interface VaultedPaymentMethodsRepository {

    // upon refactor of fragments, convert to domain model!
    suspend fun getVaultedPaymentMethods(fromCache: Boolean):
        Result<List<PaymentMethodVaultTokenInternal>>

    suspend fun deleteVaultedPaymentMethod(id: String): Result<Unit>
}
