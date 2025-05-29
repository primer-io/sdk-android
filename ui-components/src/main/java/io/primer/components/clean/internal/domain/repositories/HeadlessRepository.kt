package io.primer.components.clean.internal.domain.repositories

internal interface HeadlessRepository {

    suspend fun getAvailablePaymentMethods()

}
