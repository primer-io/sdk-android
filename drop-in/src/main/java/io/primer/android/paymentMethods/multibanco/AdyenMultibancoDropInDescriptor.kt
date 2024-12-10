@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.multibanco

import io.primer.android.PrimerSessionIntent
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.multibanko.MultibancoConditionsFragment
import io.primer.android.ui.fragments.multibanko.MultibancoPaymentFragment
import io.primer.android.viewmodel.ViewStatus
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class AdyenMultibancoDropInDescriptor(
    override val uiOptions: UiOptions,
    private val brandRegistry: BrandRegistry,
    private val sessionIntent: PrimerSessionIntent
) : PaymentMethodDropInDescriptor {

    override val paymentMethodType: String = PaymentMethodType.ADYEN_MULTIBANCO.name

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            factory = { MultibancoConditionsFragment.newInstance(sessionIntent, paymentMethodType) },
            returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not()
        )

    override val behaviours: List<PaymentMethodBehaviour>
        get() = listOf(
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance)
        )

    override fun createSuccessBehavior(viewStatus: ViewStatus.ShowSuccess): NewFragmentBehaviour =
        NewFragmentBehaviour(
            factory = {
                with(viewStatus.checkoutAdditionalInfo as MultibancoCheckoutAdditionalInfo) {
                    MultibancoPaymentFragment.newInstance(
                        entity = entity,
                        reference = reference,
                        expiresAt = expiresAt
                    )
                }
            },
            returnToPreviousOnBack = false
        )

    override val loadingState = run {
        val brand = brandRegistry.getBrand(paymentMethodType)

        LoadingState(
            imageResIs = when (uiOptions.isDarkMode) {
                true -> brand.iconDarkResId
                else -> brand.iconResId
            }
        )
    }

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM
}
