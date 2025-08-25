package io.primer.android.internal.domain.usecase

import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.first

internal class SubmitCardPaymentUseCase(
    private val rawDataManagerRepository: RawDataManagerRepository,
    private val headlessRepository: HeadlessRepository,
    private val actionInteractor: ActionInteractor,
) {

    suspend operator fun invoke(formData: Map<PrimerInputElementType, String>): Result<PrimerCheckoutData> {
        // Validate billing address if any billing fields are filled
        validateBillingAddress(formData).fold(
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

        // Wait for the result from the headless repository
        return headlessRepository.paymentResults.first()
    }

    private suspend fun validateBillingAddress(formData: Map<PrimerInputElementType, String>): Result<Unit> {
        // Check if any billing fields are filled
        val billingFields = listOf(
            PrimerInputElementType.FIRST_NAME,
            PrimerInputElementType.LAST_NAME,
            PrimerInputElementType.ADDRESS_LINE_1,
            PrimerInputElementType.ADDRESS_LINE_2,
            PrimerInputElementType.CITY,
            PrimerInputElementType.POSTAL_CODE,
            PrimerInputElementType.COUNTRY_CODE,
            PrimerInputElementType.STATE,
        )
        val hasBillingData = billingFields.any { field ->
            formData[field]?.isNotBlank() == true
        }

        if (!hasBillingData) {
            return Result.success(Unit)
        }

        val action = ActionUpdateBillingAddressParams(
            firstName = formData[PrimerInputElementType.FIRST_NAME]?.takeIf { it.isNotBlank() },
            lastName = formData[PrimerInputElementType.LAST_NAME]?.takeIf { it.isNotBlank() },
            addressLine1 = formData[PrimerInputElementType.ADDRESS_LINE_1]?.takeIf { it.isNotBlank() },
            addressLine2 = formData[PrimerInputElementType.ADDRESS_LINE_2]?.takeIf { it.isNotBlank() },
            city = formData[PrimerInputElementType.CITY]?.takeIf { it.isNotBlank() },
            postalCode = formData[PrimerInputElementType.POSTAL_CODE]?.takeIf { it.isNotBlank() },
            countryCode = formData[PrimerInputElementType.COUNTRY_CODE]?.takeIf { it.isNotBlank() },
            state = formData[PrimerInputElementType.STATE]?.takeIf { it.isNotBlank() },
        )

        return actionInteractor(MultipleActionUpdateParams(listOf(action)))
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) },
            )
    }
}
