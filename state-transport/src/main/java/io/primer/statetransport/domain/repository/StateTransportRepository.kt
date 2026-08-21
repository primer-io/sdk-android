package io.primer.statetransport.domain.repository

import io.primer.statetransport.domain.model.InstructionFetch

interface StateTransportRepository {

    suspend fun start(
        clientSessionId: String,
        paymentMethodType: String,
        returnUri: String,
    ): Result<InstructionFetch>

    suspend fun fetchInstructions(
        clientSessionId: String,
    ): Result<InstructionFetch>
}
