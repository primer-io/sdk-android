package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.components.ui.assets.PrimerAssetManager
import io.primer.android.components.ui.views.PaymentMethodViewCreator
import io.primer.android.databinding.FragmentPaymentMethodLoadingBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.scaleImage
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.dPtoPx
import io.primer.android.utils.toResourcesScale
import io.primer.android.viewmodel.PrimerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.inject

@ExperimentalCoroutinesApi
internal open class PaymentMethodLoadingFragment : Fragment(), DIAppComponent {

    private val viewModel: PrimerViewModel by activityViewModels()
    private val theme: PrimerTheme by inject()

    private var binding: FragmentPaymentMethodLoadingBinding by autoCleaned()

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> { descriptor ->
        descriptor?.getLoadingState().let { loadingState ->
            logAnalytics(descriptor.config.type)
            binding.apply {
                if (loadingState != null && loadingState.imageResIs > 0) {
                    selectedPaymentLogo.setImageResource(loadingState.imageResIs)
                } else {
                    selectedPaymentLogo.setImageDrawable(
                        PrimerAssetManager.getAsset(
                            requireContext(),
                            descriptor.config.type,
                            when (theme.isDarkMode == true) {
                                true -> ImageColor.DARK
                                false -> ImageColor.LIGHT
                            }
                        )?.scaleImage(
                            requireContext(),
                            requireContext().resources.displayMetrics.toResourcesScale() /
                                PaymentMethodViewCreator.DEFAULT_EXPORTED_ICON_SCALE,
                            maxHeight = PaymentMethodViewCreator.DEFAULT_EXPORTED_ICON_MAX_HEIGHT
                                .dPtoPx(requireContext())
                        )
                    )
                }
                loadingState?.textResId?.let {
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
        binding = FragmentPaymentMethodLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedPaymentMethod.observe(viewLifecycleOwner, selectedPaymentMethodObserver)
    }

    private fun logAnalytics(type: String) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.LOADER,
                Place.PAYMENT_METHOD_LOADING,
                null,
                PaymentMethodContextParams(type)
            )
        )

    companion object {

        fun newInstance(): PaymentMethodLoadingFragment {
            return PaymentMethodLoadingFragment()
        }
    }
}
