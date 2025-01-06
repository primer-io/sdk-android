@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.core.ui.descriptors

import io.primer.android.assets.ui.model.Brand
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal abstract class TestDropInPaymentMethodDescriptor(
    override val paymentMethodType: String,
    override val uiOptions: UiOptions,
    val brand: Brand,
) : PaymentMethodDropInDescriptor {
    override val behaviours: List<PaymentMethodBehaviour>
        get() =
            if (uiOptions.isInitScreenEnabled.not() && uiOptions.isStandalonePaymentMethod) {
                emptyList()
            } else {
                listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance))
            }

    override val loadingState: LoadingState
        get() = LoadingState(brand.logoResId)
}
