package io.primer.android.payment.async.webRedirect

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.payment.LoadingState

internal class WebRedirectPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config), DISdkComponent {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        SelectedPaymentMethodManagerBehaviour(options.type, localConfig.paymentMethodIntent)

    override fun getLoadingState() = LoadingState(brand.logoResId)

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))
}
