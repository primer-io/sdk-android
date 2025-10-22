package io.primer.android.internal.domain.usecase.vault

import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Use case to determine if CVV should be captured for a vaulted payment method.
 * Checks the configuration setting for captureVaultedCardCvv and the payment method type.
 */
internal class ShouldCaptureVaultedCvvUseCase(
    private val configurationInteractor: ConfigurationInteractor,
) {

    /**
     * Determines if CVV capture is required for the given vaulted payment method.
     *
     * @param vaultedPaymentMethod The vaulted payment method to check
     * @return Result containing true if CVV should be captured, false otherwise
     */
    suspend operator fun invoke(vaultedPaymentMethod: PrimerVaultedPaymentMethod): Result<Boolean> {
        return configurationInteractor(ConfigurationParams(CachePolicy.ForceCache)).mapCatching { configuration ->
            if (vaultedPaymentMethod.paymentMethodType != PaymentMethodType.PAYMENT_CARD.name) {
                return@mapCatching false
            }

            val captureVaultedCardCvv = configuration.paymentMethods
                .find { it.type == PaymentMethodType.PAYMENT_CARD.name }
                ?.options
                ?.captureVaultedCardCvv

            captureVaultedCardCvv == true
        }
    }
}
