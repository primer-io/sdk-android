package io.primer.android.internal.domain.usecases

import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.models.CardFormData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.first

internal class SubmitCardPaymentUseCase : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val headlessRepository: HeadlessRepository by lazy { resolve() }
    private val actionInteractor: ActionInteractor by lazy { resolve() }

    suspend operator fun invoke(formData: CardFormData): Result<PrimerCheckoutData> {
        // Validate billing address if any billing fields are filled
        validateBillingAddress(formData).fold(
            onSuccess = {
                // Billing address validation passed, proceed with submission
                rawDataManagerRepository.submit()
            },
            onFailure = { error ->
                // Billing address validation failed
                return Result.failure(
                    Exception("Billing address validation failed: ${error.message}")
                )
            }
        )

        // Wait for the result from the headless repository
        return headlessRepository.paymentResults.first()
    }

    private suspend fun validateBillingAddress(formData: CardFormData): Result<Unit> {
        // Check if any billing fields are filled
        val hasBillingData = listOf(
            formData.firstName,
            formData.lastName,
            formData.addressLine1,
            formData.addressLine2,
            formData.city,
            formData.postalCode,
            formData.countryCode,
            formData.state
        ).any { it.isNotBlank() }

        if (!hasBillingData) {
            return Result.success(Unit)
        }

        return try {
            val action = ActionUpdateBillingAddressParams(
                firstName = formData.firstName.takeIf { it.isNotBlank() },
                lastName = formData.lastName.takeIf { it.isNotBlank() },
                addressLine1 = formData.addressLine1.takeIf { it.isNotBlank() },
                addressLine2 = formData.addressLine2.takeIf { it.isNotBlank() },
                city = formData.city.takeIf { it.isNotBlank() },
                postalCode = formData.postalCode.takeIf { it.isNotBlank() },
                countryCode = formData.countryCode.takeIf { it.isNotBlank() },
                state = formData.state.takeIf { it.isNotBlank() }
            )

            actionInteractor(MultipleActionUpdateParams(listOf(action)))
                .fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { Result.failure(it) }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}