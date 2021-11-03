package io.primer.android.domain.payments.methods.repository

import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.Flow

internal interface VaultedPaymentMethodsRepository {

    // upon refactor of fragments, convert to domain model!
    fun getVaultedPaymentMethods(): Flow<List<PaymentMethodTokenInternal>>
}
