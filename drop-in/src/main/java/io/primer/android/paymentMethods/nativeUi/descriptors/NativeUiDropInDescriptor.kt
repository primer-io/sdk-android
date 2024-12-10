@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.nativeUi.descriptors

import io.primer.android.PrimerSessionIntent
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NativeUiPaymentMethodManagerCancellationBehaviour
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.NativeUiSelectedPaymentMethodManagerBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal data class NativeUiDropInDescriptor(
    override val paymentMethodType: String,
    override val uiOptions: UiOptions,
    private val primerSessionIntent: PrimerSessionIntent,
    private val brandRegistry: BrandRegistry
) : PaymentMethodDropInDescriptor {

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NativeUiSelectedPaymentMethodManagerBehaviour(
            paymentMethodType = paymentMethodType,
            sessionIntent = primerSessionIntent
        )

    override val behaviours: List<PaymentMethodBehaviour>
        get() = if (uiOptions.isInitScreenEnabled.not() &&
            uiOptions.isStandalonePaymentMethod
        ) {
            listOf()
        } else {
            listOf(NewFragmentBehaviour({ PaymentMethodLoadingFragment.newInstance(popBackStackToRoot = true) }))
        }

    override val cancelBehaviour: NativeUiPaymentMethodManagerCancellationBehaviour
        get() = NativeUiPaymentMethodManagerCancellationBehaviour()

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val loadingState: LoadingState
        get() = LoadingState(brandRegistry.getBrand(paymentMethodType = paymentMethodType).logoResId)
}
