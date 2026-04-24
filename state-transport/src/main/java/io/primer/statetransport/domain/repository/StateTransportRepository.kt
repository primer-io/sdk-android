package io.primer.statetransport.domain.repository

import io.primer.statetransport.domain.model.ClientInstructions

interface StateTransportRepository {

    suspend fun start(
        clientSessionId: String,
        paymentMethodType: String,
        returnUri: String,
    ): Result<ClientInstructions>

    suspend fun fetchInstructions(
        clientSessionId: String,
    ): Result<ClientInstructions>
}
