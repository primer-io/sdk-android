package io.primer.checkout.orchestrator.domain.repository

import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck

/** Whether this SDK meets the client requirements of the configuration's backend-driven methods. */
internal interface ClientRequirementsRepository {
    /** Verdicts by payment method type. */
    suspend fun getVerdicts(): Result<Map<String, ClientRequirementsCheck>>
}
