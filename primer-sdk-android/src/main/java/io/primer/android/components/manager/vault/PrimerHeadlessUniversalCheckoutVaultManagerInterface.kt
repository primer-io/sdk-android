package io.primer.android.components.manager.vault

import io.primer.android.components.domain.exception.VaultManagerDeleteException
import io.primer.android.components.domain.exception.VaultManagerFetchException
import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethodData

interface PrimerHeadlessUniversalCheckoutVaultManagerInterface {

    /**
     * Retrieves a list of vaulted payment methods from the Primer API.
     *
     * @return A [Result] object containing the list of [PrimerVaultedPaymentMethodData]
     * if the operation was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [VaultManagerFetchException] if an error occurs while fetching the vaulted payment methods.
     * - [IOException] if an I/O error occurs during the API request.
     */
    suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethodData>>

    /**
     * Delete a vaulted payment method by vaultedPaymentMethodId. You can get the ID from any instance
     * of [PrimerVaultedPaymentMethodData].
     *
     * @param vaultedPaymentMethodId The ID of the vaulted payment method to be deleted.
     * @return A [Result] object representing the operation's result. It contains [Unit]
     * if the deletion was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     * - [VaultManagerDeleteException] if an error occurs while fetching the vaulted payment methods.
     * - [IOException] if an I/O error occurs during the API request.
     */
    suspend fun deleteVaultedPaymentMethod(vaultedPaymentMethodId: String): Result<Unit>

    /**
     * Initiates the payment flow using the specified vaulted payment method.
     *
     * @param vaultedPaymentMethodId The ID of the vaulted payment method to use for the payment flow.
     * @return A [Result] object representing the operation's result. It contains [Unit]
     * if the starting payment flow was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     */
    suspend fun startPaymentFlow(vaultedPaymentMethodId: String): Result<Unit>
}
