package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.databinding.PrimerFragmentPaymentMethodLoadingBinding
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.paymentMethods.core.ui.assets.AssetsManager
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getParentDialogOrNull
import io.primer.android.ui.extensions.popBackStackToRoot
import io.primer.android.ui.fragments.base.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal open class PaymentMethodLoadingFragment : BaseFragment(), DISdkComponent {
    private val assetsManager: AssetsManager by inject()

    private var binding: PrimerFragmentPaymentMethodLoadingBinding by autoCleaned()

    private val selectedPaymentMethodObserver =
        Observer<PaymentMethodDropInDescriptor?> { descriptor ->
            descriptor?.loadingState?.let { loadingState ->
                logAnalytics(descriptor.paymentMethodType)
                binding.apply {
                    if (loadingState.imageResIs > 0) {
                        selectedPaymentLogo.setImageResource(loadingState.imageResIs)
                    } else {
                        selectedPaymentLogo.setImageDrawable(
                            assetsManager.getPaymentMethodImage(
                                context = requireContext(),
                                paymentMethodType = descriptor.paymentMethodType,
                                imageColor =
                                when (theme.isDarkMode == true) {
                                    true -> ImageColor.DARK
                                    false -> ImageColor.LIGHT
                                },
                            ),
                        )
                    }
                    loadingState.textResId?.let {
                        selectedPaymentLoadingText.isVisible = true
                        progressBar.isVisible = false
                        selectedPaymentLoadingText.text = loadingState.args
                            ?.let { args -> getString(it, args) } ?: getString(it)
                    } ?: run {
                        selectedPaymentLoadingText.isVisible = false
                        progressBar.isVisible = true
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentPaymentMethodLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        getToolbar()?.isVisible = false

        if (arguments?.getBoolean(POP_BACK_STACK_TO_ROOT_KEY, false) == true) {
            addOnBackPressedCallback()
        }

        primerViewModel.selectedPaymentMethod.observe(viewLifecycleOwner, selectedPaymentMethodObserver)
    }

    //region Utils
    private fun addOnBackPressedCallback() {
        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback(this) {
            (parentFragment as? CheckoutSheetFragment)?.popBackStackToRoot()
            primerViewModel.selectedPaymentMethod.value?.cancelBehaviour?.invoke(primerViewModel)
        }
    }

    private fun logAnalytics(type: String) =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.LOADER,
                Place.PAYMENT_METHOD_LOADING,
                null,
                PaymentMethodContextParams(type),
            ),
        )
    //endregion

    companion object {
        private const val POP_BACK_STACK_TO_ROOT_KEY = "pop_back_stack_to_root"

        fun newInstance(): PaymentMethodLoadingFragment =
            PaymentMethodLoadingFragment().apply {
                arguments = bundleOf(POP_BACK_STACK_TO_ROOT_KEY to false)
            }

        /**
         * Returns a new instance of [PaymentMethodLoadingFragment]. If [popBackStackToRoot] is set to true, a
         * back press handler will be defined that pops the entire backstack of the host fragment, otherwise, default
         * back handling will be used.
         */
        fun newInstance(popBackStackToRoot: Boolean): PaymentMethodLoadingFragment =
            PaymentMethodLoadingFragment().apply {
                arguments = bundleOf(POP_BACK_STACK_TO_ROOT_KEY to popBackStackToRoot)
            }
    }
}
