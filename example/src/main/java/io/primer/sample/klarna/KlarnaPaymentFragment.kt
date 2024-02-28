package io.primer.sample.klarna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.manager.klarna.KlarnaHeadlessManager
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.PrimerKlarnaPaymentView
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.KlarnaPaymentComponent
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData.PaymentCategory.ReturnIntentData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.sample.databinding.FragmentKlarnaBinding
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KlarnaPaymentFragment : Fragment() {
    private val headlessManagerViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]
    }

    private lateinit var binding: FragmentKlarnaBinding

    private lateinit var klarnaPaymentComponent: KlarnaPaymentComponent

    private val primerKlarnaPaymentView
        get() = binding.klarnaPaymentViewContainer.allViews.filterIsInstance<PrimerKlarnaPaymentView>()
            .firstOrNull()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentKlarnaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is UiState.ShowError -> findNavController().popBackStack()
                        else -> Unit
                    }
                }
            }
        }

        klarnaPaymentComponent =
            KlarnaHeadlessManager().provideKlarnaPaymentComponent(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectKlarnaPaymentComponentSteps() }
                launch { collectComponentErrors() }
                launch { collectValidationStatuses() }
            }
        }

        klarnaPaymentComponent.start()
    }

    private suspend fun collectKlarnaPaymentComponentSteps() {
        klarnaPaymentComponent.componentStep.collectLatest { klarnaStep ->
            when (klarnaStep) {
                is KlarnaPaymentStep.PaymentSessionCreated -> {
                    klarnaStep.paymentCategories.forEach {
                        binding.paymentCategoryGroup.addView(
                            RadioButton(requireContext()).apply {
                                text = it.name
                                id = View.generateViewId()
                                tag = it
                            }
                        )
                    }
                }

                is KlarnaPaymentStep.PaymentViewLoaded -> {
                    with(binding) {
                        klarnaPaymentViewContainer.addView(
                            klarnaStep.paymentView
                        )
                        authorize.visibility = View.VISIBLE
                    }
                }

                is KlarnaPaymentStep.PaymentAuthorizationRequired -> {
                    primerKlarnaPaymentView?.authorize()
                }

                is KlarnaPaymentStep.PaymentSessionAuthorized -> {
                    if (klarnaStep.isFinalized) {
                        findNavController().popBackStack()
                    }
                }

                is KlarnaPaymentStep.PaymentSessionFinalized -> {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private suspend fun collectComponentErrors() {
        klarnaPaymentComponent.componentError.collectLatest {
            Snackbar.make(requireView(), it.description, Snackbar.LENGTH_SHORT).show()
        }
    }

    private suspend fun collectValidationStatuses() {
        klarnaPaymentComponent.componentValidationStatus.collectLatest {
            if (it is PrimerValidationStatus.Error || it is PrimerValidationStatus.Invalid) {
                Snackbar.make(requireView(), it.toString(), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        binding.initializeKlarnaView.setOnClickListener {
            val checkedRadioButton =
                binding.paymentCategoryGroup.findViewById<RadioButton>(
                    binding.paymentCategoryGroup.checkedRadioButtonId
                )

            klarnaPaymentComponent.updateCollectedData(
                KlarnaPaymentCollectableData.PaymentCategory(
                    context = requireContext(),
                    paymentCategory = checkedRadioButton.tag as KlarnaPaymentCategory,
                    returnIntentData = ReturnIntentData(
                        scheme = "app",
                        host = "deeplink.return.activity"
                    )
                )
            )
        }

        binding.authorize.setOnClickListener {
            klarnaPaymentComponent.submit()
        }
    }
}
