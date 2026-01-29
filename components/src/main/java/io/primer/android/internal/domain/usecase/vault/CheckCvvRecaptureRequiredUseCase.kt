package io.primer.android.internal.domain.usecase.vault

import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class CheckCvvRecaptureRequiredUseCase(
    private val configurationInteractor: ConfigurationInteractor,
) {
    suspend operator fun invoke(paymentMethod: PrimerVaultedPaymentMethod) = configurationInteractor(
        ConfigurationParams(
            CachePolicy.ForceCache,
        ),
    )
        .mapCatching { config ->
            config.paymentMethods
                .find { it.type == PaymentMethodType.PAYMENT_CARD.name }
                ?.options
                ?.captureVaultedCardCvv == true
        }
        .getOrDefault(false)
}
