package io.primer.android.components.assets.displayMetadata

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.components.assets.displayMetadata.repository.PaymentMethodImplementationRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.core.domain.None

class PaymentMethodsImplementationInteractor(
    private val paymentMethodImplementationRepository: PaymentMethodImplementationRepository,
    private val analyticsRepository: AnalyticsRepository,
) : BaseInteractor<List<PaymentMethodImplementation>, None>() {
    override fun execute(params: None): List<PaymentMethodImplementation> {
        val paymentMethodsImplementation =
            paymentMethodImplementationRepository.getPaymentMethodsImplementation()
        val filteredPaymentMethodsImplementation =
            paymentMethodsImplementation
                .filter { paymentMethodImplementation ->
                    val iconMetadata =
                        paymentMethodImplementation.buttonMetadata
                            ?.iconDisplayMetadata.orEmpty()
                    iconMetadata.isNotEmpty() &&
                        iconMetadata.map {
                            it.iconResId > 0 || it.filePath.isNullOrBlank().not()
                        }.all { it }
                }

        val difference =
            paymentMethodsImplementation.minus(
                filteredPaymentMethodsImplementation.toSet(),
            )
        difference.forEach {
            logAnalyticsEvent(it.paymentMethodType)
        }
        return filteredPaymentMethodsImplementation
    }

    private fun logAnalyticsEvent(paymentMethodType: String) =
        analyticsRepository.addEvent(
            MessageAnalyticsParams(
                MessageType.PM_IMAGE_LOADING_FAILED,
                "Failed to load icon assets for $paymentMethodType.",
                Severity.ERROR,
            ),
        )
}
