package io.primer.android.components.manager.vault

import io.primer.android.components.domain.exception.VaultManagerDeleteException
import io.primer.android.components.domain.exception.VaultManagerFetchException
import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

interface PrimerHeadlessUniversalCheckoutVaultManagerInterface {

    /**
     * Retrieves a list of vaulted payment methods from the Primer API.
     *
     * @return A [Result] object containing the list of [PrimerVaultedPaymentMethod]
     * if the operation was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [VaultManagerFetchException] if an error occurs while fetching the vaulted payment methods.
     * - [IOException] if an I/O error occurs during the API request.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("fetchVaultedPaymentMethods")
    suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethod>>

    /**
     * Delete a vaulted payment method by vaultedPaymentMethodId. You can get the ID from any instance
     * of [PrimerVaultedPaymentMethod].
     *
     * @param vaultedPaymentMethodId The ID of the vaulted payment method to be deleted.
     * @return A [Result] object representing the operation's result. It contains [Unit]
     * if the deletion was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     * - [VaultManagerDeleteException] if an error occurs while deleting the vaulted payment method.
     * - [IOException] if an I/O error occurs during the API request.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("deleteVaultedPaymentMethod")
    suspend fun deleteVaultedPaymentMethod(vaultedPaymentMethodId: String): Result<Unit>

    /**
     * Validate [PrimerVaultedPaymentMethodAdditionalData] that can be passed to [startPaymentFlow].
     *
     * @param vaultedPaymentMethodId that can be retrieved from [PrimerVaultedPaymentMethod]
     * @param additionalData that's an instance of [PrimerVaultedPaymentMethodAdditionalData]
     * @return A [Result] object representing the operation's result. It contains List<[PrimerValidationError]>
     * if SDK was able to perform validation, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("validate")
    suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData
    ): Result<List<PrimerValidationError>>

    /**
     * Initiates the payment flow using the specified vaulted payment method.
     *
     * @param vaultedPaymentMethodId The ID of the vaulted payment method to use for the payment flow.
     * @return A [Result] object representing the operation's result. It contains [Unit]
     * if the starting payment flow was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("startPaymentFlow")
    suspend fun startPaymentFlow(vaultedPaymentMethodId: String): Result<Unit>

    /**
     * Initiates the payment flow using the specified vaulted payment method and additional data needed for the payment.
     *
     * @param vaultedPaymentMethodId The ID of the vaulted payment method to use for the payment flow.
     * @param additionalData The additional data needed for the payment flow.
     * @return A [Result] object representing the operation's result. It contains [Unit]
     * if the starting payment flow was successful, or an error if the operation failed.
     * In case of error, as part of [Result] object, SDK will return:
     * - [InvalidVaultedPaymentMethodIdException] if [vaultedPaymentMethodId] does not exists.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("startPaymentFlow")
    suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData
    ): Result<Unit>
}
