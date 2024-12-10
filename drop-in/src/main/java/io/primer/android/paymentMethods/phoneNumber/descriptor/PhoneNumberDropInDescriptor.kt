@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.phoneNumber.descriptor

import androidx.annotation.VisibleForTesting
import io.primer.android.R
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal data class PhoneNumberDropInDescriptor(
    override val paymentMethodType: String,
    override val uiOptions: UiOptions,
    private val brandRegistry: BrandRegistry,
    private val paymentMethodName: String?
) : PaymentMethodDropInDescriptor {

    @VisibleForTesting
    val fragmentFactory = when (paymentMethodType) {
        PaymentMethodType.ADYEN_MBWAY.name -> DynamicFormFragment::newInstance

        else -> error("Unsupported payment method type '$paymentMethodType'")
    }

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            factory = fragmentFactory,
            returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not()
        )

    override val behaviours: List<PaymentMethodBehaviour> = emptyList()

    override fun createPollingStartedBehavior(viewStatus: ViewStatus.PollingStarted): NewFragmentBehaviour =
        NewFragmentBehaviour(
            factory = { PaymentMethodLoadingFragment.newInstance(popBackStackToRoot = true) },
            replacePreviousFragment = false
        )

    override val loadingState = run {
        val brand = brandRegistry.getBrand(paymentMethodType)

        LoadingState(
            imageResIs = when (uiOptions.isDarkMode) {
                true -> brand.iconDarkResId
                else -> brand.iconResId
            },
            textResId = R.string.completeYourPaymentInTheApp,
            args = paymentMethodName.orEmpty()
        )
    }

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM
}
