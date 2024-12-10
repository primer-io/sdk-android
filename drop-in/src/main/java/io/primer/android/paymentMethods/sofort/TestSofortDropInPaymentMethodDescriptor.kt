@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.sofort

import io.primer.android.assets.ui.model.Brand
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.TestDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.ui.fragments.processorTest.ProcessorTestResultSelectorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class TestSofortDropInPaymentMethodDescriptor(
    paymentMethodType: String,
    uiOptions: UiOptions,
    brand: Brand
) : TestDropInPaymentMethodDescriptor(paymentMethodType, uiOptions, brand) {

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            ProcessorTestResultSelectorFragment::newInstance,
            returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not()
        )

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON
}
