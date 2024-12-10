@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.bancontact

import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.bancontact.BancontactCardFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class AdyenBancontactDropInDescriptor(
    override val uiOptions: UiOptions,
    private val brandRegistry: BrandRegistry
) : PaymentMethodDropInDescriptor {

    override val paymentMethodType: String = PaymentMethodType.ADYEN_BANCONTACT_CARD.name

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            factory = BancontactCardFragment::newInstance,
            returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not()
        )

    override val behaviours: List<PaymentMethodBehaviour>
        get() = listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance))

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
