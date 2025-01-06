package io.primer.android.ui.fragments.processorTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.ProcessorTestDecisionParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.databinding.FragmentProcessorTestResultSelectorBinding
import io.primer.android.di.ProcessorTestContainer
import io.primer.android.paymentMethods.core.ui.descriptors.TestDropInPaymentMethodDescriptor
import io.primer.android.sandboxProcessor.SandboxProcessorDecisionType
import io.primer.android.sandboxProcessor.implementation.components.ProcessorTestCollectableData
import io.primer.android.sandboxProcessor.implementation.components.ProcessorTestComponent
import io.primer.android.sandboxProcessor.implementation.components.ProcessorTestStep
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class ProcessorTestResultSelectorFragment : Fragment(), DISdkComponent {
    private val theme: PrimerTheme by inject()

    private lateinit var processorTestComponent: ProcessorTestComponent

    private val primerViewModel: PrimerViewModel by activityViewModels()

    private val viewModel: ProcessorTestResultSelectorViewModel
        by viewModel<ProcessorTestResultSelectorViewModel, ProcessorTestResultSelectorViewModelFactory>()

    private var binding: FragmentProcessorTestResultSelectorBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        DISdkContext.dropInSdkContainer?.apply {
            registerContainer(ProcessorTestContainer(this))
        }
        processorTestComponent = ProcessorTestComponent.provideInstance()
        binding =
            FragmentProcessorTestResultSelectorBinding.inflate(
                inflater,
                container,
                false,
            )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DISdkContext.dropInSdkContainer?.apply {
            unregisterContainer<ProcessorTestContainer>()
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrentPaymentMethod()
        setupPayButton()
        setupTheme()
        setupListeners()
    }

    private fun setupPayButton() {
        binding.btnPay.amount = primerViewModel.getTotalAmountFormatted()
    }

    private fun setupCurrentPaymentMethod() {
        val descriptor = primerViewModel.selectedPaymentMethod.value as? TestDropInPaymentMethodDescriptor ?: return
        binding.ivPaymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) {
                descriptor.brand.iconDarkResId
            } else {
                descriptor.brand.iconLightResId
            },
        )
    }

    private fun setupTheme() {
        binding.tvDummyInfo.setTextColor(
            theme.subtitleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
        binding.ivDummyResultBack.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
        binding.rbAuthorized.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
        binding.rbDeclined.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
        binding.rbFailed.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode,
            ),
        )
    }

    private fun setupListeners() {
        binding.btnPay.setOnClickListener {
            binding.btnPay.isEnabled = false

            lifecycleScope.launch {
                /*
                Wait for tokenization to complete so that the selected payment method is not overriden by the
                payment method selection button. The old implementation works without this because tokenization
                is done in the ViewModel's scope, which uses the `immediate` main dispatcher.
                 */
                processorTestComponent.componentStep.flowWithLifecycle(lifecycle)
                    .filterIsInstance<ProcessorTestStep.Tokenized>()
                    .collectLatest { parentFragmentManager.popBackStack() }
            }

            val descriptor =
                primerViewModel
                    .selectedPaymentMethod.value ?: return@setOnClickListener

            val decisionType = getProcessorTestDecisionType()

            with(processorTestComponent) {
                updateCollectedData(
                    ProcessorTestCollectableData(decisionType = decisionType),
                )
                start(
                    paymentMethodType = descriptor.paymentMethodType,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
            }
        }
        binding.ivDummyResultBack.setOnClickListener {
            logAnalyticsBackPressed()
            goOnSelectedPaymentMethod()
        }
    }

    private fun goOnSelectedPaymentMethod() {
        primerViewModel.setViewStatus(ViewStatus.SelectPaymentMethod)
    }

    private fun getProcessorTestDecisionType(): SandboxProcessorDecisionType {
        return SandboxProcessorDecisionType.valueOf(
            binding.root.findViewById<RadioButton>(
                binding.rgResponseType.checkedRadioButtonId,
            ).tag as String,
        )
    }

    private fun logAnalyticsBackPressed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                action = AnalyticsAction.CLICK,
                objectType = ObjectType.BUTTON,
                place = Place.PRIMER_TEST_PAYMENT_METHOD_DECISION_SCREEN,
                objectId = ObjectId.BACK,
                context = ProcessorTestDecisionParams(getProcessorTestDecisionType().name),
            ),
        )

    companion object {
        fun newInstance() = ProcessorTestResultSelectorFragment()
    }
}
