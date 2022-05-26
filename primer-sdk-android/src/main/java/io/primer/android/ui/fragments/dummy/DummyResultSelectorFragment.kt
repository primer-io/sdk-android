package io.primer.android.ui.fragments.dummy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.PrimerTheme
import io.primer.android.databinding.FragmentDummyResultSelectorBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.OnActionContinueCallback
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.dummy.DummyDecisionType
import io.primer.android.payment.dummy.DummyResultDescriptorHandler
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class DummyResultSelectorFragment : Fragment(), OnActionContinueCallback, DIAppComponent {

    private val localConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()
    private val tokenizationViewModel: TokenizationViewModel by activityViewModels()
    private val primerViewModel: PrimerViewModel by activityViewModels()

    private var binding: FragmentDummyResultSelectorBinding by autoCleaned()

    private var onActionContinue: (() -> SelectedPaymentMethodBehaviour?)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDummyResultSelectorBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTokenizeObserver()
        setupCurrentPaymentMethod()
        setupPayButton()
        setupTheme()
        setupListeners()
    }

    private fun setupTokenizeObserver() {
        tokenizationViewModel.tokenizationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                TokenizationStatus.LOADING -> binding.btnPay.showProgress()
                TokenizationStatus.SUCCESS -> onActionContinue?.invoke()?.let { behaviour ->
                    primerViewModel.executeBehaviour(behaviour)
                }
            }
        }
    }

    private fun setupPayButton() {
        binding.btnPay.amount = localConfig.monetaryAmount
    }

    private fun setupCurrentPaymentMethod() {
        val descriptor = primerViewModel.selectedPaymentMethod.value ?: return
        binding.ivPaymentMethodIcon.setImageResource(
            if (theme.isDarkMode == true) descriptor.brand.iconDarkResId
            else descriptor.brand.iconLightResId
        )
    }

    private fun setupTheme() {
        binding.tvDummyInfo.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.ivDummyResultBack.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.rbAuthorized.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.rbDeclined.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        binding.rbFailed.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
    }

    private fun setupListeners() {
        binding.btnPay.setOnClickListener {
            parentFragmentManager.popBackStack()
            val descriptor = primerViewModel
                .selectedPaymentMethod.value ?: return@setOnClickListener

            val selectedDecision = findSelectedItemTag()
            when (descriptor) {
                is DummyResultDescriptorHandler -> {
                    descriptor.setDecision(selectedDecision)
                }
                else -> throw IllegalStateException(
                    "Your selected payment method, not support dummy result for $descriptor"
                )
            }

            tokenizationViewModel.resetPaymentMethod(descriptor)
            tokenizationViewModel.tokenize()
        }
        binding.ivDummyResultBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun findSelectedItemTag(): DummyDecisionType {
        return DummyDecisionType.valueOf(
            binding.root.findViewById<RadioButton>(
                binding.rgResponseType.checkedRadioButtonId
            ).tag as String
        )
    }

    companion object {

        fun newInstance() = DummyResultSelectorFragment()
    }

    override fun onProvideActionContinue(onAction: () -> SelectedPaymentMethodBehaviour?) {
        this.onActionContinue = onAction
    }
}
