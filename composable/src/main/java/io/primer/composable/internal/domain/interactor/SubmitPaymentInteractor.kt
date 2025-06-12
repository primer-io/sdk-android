package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.domain.PrimerCheckoutData
import io.primer.composable.internal.domain.repositories.HeadlessRepository
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.first

class SubmitPaymentInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val headlessRepository: HeadlessRepository by lazy { resolve() }
    private val validateBillingAddressInteractor: ValidateBillingAddressInteractor by lazy { resolve() }

    suspend operator fun invoke(inputFields: Map<PrimerInputElementType, String>): Result<PrimerCheckoutData> {
        // Validate billing address first (if required)
        validateBillingAddressInteractor(inputFields).fold(
            onSuccess = {
                // Billing address validation passed, proceed with submission
                rawDataManagerRepository.submit()
            },
            onFailure = { error ->
                // Billing address validation failed
                return Result.failure(
                    Exception("Billing address validation failed: ${error.message}"),
                )
            },
        )

        // Wait for the result from the headless repository listener
        return headlessRepository.paymentResults.first()
    }
}
