package io.primer.composable.internal.domain.interactor

import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class ValidateBillingAddressInteractor : DISdkComponent {

    private val actionInteractor: ActionInteractor by lazy { resolve() }

    suspend operator fun invoke(inputFields: Map<PrimerInputElementType, String>): Result<Unit> {
        return try {
            // Filter billing address fields from input data
            val billingAddressFields = listOf(
                PrimerInputElementType.FIRST_NAME,
                PrimerInputElementType.LAST_NAME,
                PrimerInputElementType.ADDRESS_LINE_1,
                PrimerInputElementType.ADDRESS_LINE_2,
                PrimerInputElementType.CITY,
                PrimerInputElementType.POSTAL_CODE,
                PrimerInputElementType.COUNTRY_CODE,
                PrimerInputElementType.STATE,
            )

            val billingAddressData = inputFields.filterKeys { it in billingAddressFields }

            // If no billing address fields are filled, validation passes
            if (billingAddressData.isEmpty() || billingAddressData.values.all { it.isBlank() }) {
                return Result.success(Unit)
            }

            val action = ActionUpdateBillingAddressParams(
                firstName = billingAddressData[PrimerInputElementType.FIRST_NAME],
                lastName = billingAddressData[PrimerInputElementType.LAST_NAME],
                addressLine1 = billingAddressData[PrimerInputElementType.ADDRESS_LINE_1],
                addressLine2 = billingAddressData[PrimerInputElementType.ADDRESS_LINE_2],
                city = billingAddressData[PrimerInputElementType.CITY],
                postalCode = billingAddressData[PrimerInputElementType.POSTAL_CODE],
                countryCode = billingAddressData[PrimerInputElementType.COUNTRY_CODE],
                state = billingAddressData[PrimerInputElementType.STATE],
            )

            actionInteractor(MultipleActionUpdateParams(listOf(action)))
                .fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { Result.failure(it) },
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
