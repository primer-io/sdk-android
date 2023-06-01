package io.primer.android.domain.payments.methods.repository

import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal interface VaultedPaymentMethodsRepository {

    // upon refactor of fragments, convert to domain model!
    suspend fun getVaultedPaymentMethods(fromCache: Boolean):
        Result<List<PaymentMethodVaultTokenInternal>>

    fun exchangeVaultedPaymentToken(id: String): Flow<PaymentMethodTokenInternal>

    suspend fun deleteVaultedPaymentMethod(id: String): Result<Unit>
}
