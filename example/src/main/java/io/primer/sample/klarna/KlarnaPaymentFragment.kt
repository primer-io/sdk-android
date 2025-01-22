package io.primer.sample.klarna

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.sample.R
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
            HeadlessManagerViewModelFactory(AppApiKeyRepository())
        )[HeadlessManagerViewModel::class.java]
    }

    private lateinit var binding: FragmentKlarnaBinding

    private lateinit var klarnaComponent: KlarnaComponent

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

        setupListeners()
        setupObservers()

        klarnaComponent =
            PrimerHeadlessUniversalCheckoutKlarnaManager(
                viewModelStoreOwner = this
            ).provideKlarnaComponent(
                primerSessionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireNotNull(
                        arguments?.getSerializable(
                            PRIMER_SESSION_INTENT_ARG,
                            PrimerSessionIntent::class.java
                        )
                    )
                } else {
                    arguments?.getSerializable(PRIMER_SESSION_INTENT_ARG) as PrimerSessionIntent
                }
            )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectKlarnaComponentSteps() }
                launch { collectComponentErrors() }
                launch { collectValidationStatuses() }
            }
        }

        klarnaComponent.start()
    }

    private suspend fun collectKlarnaComponentSteps() {
        klarnaComponent.componentStep.collectLatest { klarnaStep ->
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

                is KlarnaPaymentStep.PaymentSessionAuthorized -> {
                    if (klarnaStep.isFinalized) {
                        // no-op
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Finalizing in 2 seconds",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        requireView().postDelayed(2000) {
                            klarnaComponent.updateCollectedData(
                                KlarnaPaymentCollectableData.FinalizePayment
                            )
                        }
                    }
                }

                is KlarnaPaymentStep.PaymentSessionFinalized -> {
                    // no-op
                }
            }
        }
    }

    private suspend fun collectComponentErrors() {
        klarnaComponent.componentError.collectLatest {
            Snackbar.make(requireView(), it.description, Snackbar.LENGTH_SHORT).show()
        }
    }

    private suspend fun collectValidationStatuses() {
        klarnaComponent.componentValidationStatus.collectLatest {
            if (it is PrimerValidationStatus.Error || it is PrimerValidationStatus.Invalid) {
                Snackbar.make(requireView(), it.toString(), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.initializeKlarnaView.setOnClickListener {
            val checkedRadioButton =
                binding.paymentCategoryGroup.findViewById<RadioButton>(
                    binding.paymentCategoryGroup.checkedRadioButtonId
                )

            klarnaComponent.updateCollectedData(
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = requireContext(),
                    paymentCategory = checkedRadioButton.tag as KlarnaPaymentCategory,
                    returnIntentUrl = Uri.Builder()
                        .scheme("app")
                        .authority("deeplink.return.activity")
                        .build()
                        .toString()
                )
            )
        }

        binding.authorize.setOnClickListener {
            klarnaComponent.submit()
        }
    }

    private fun setupObservers() {
        headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.ShowError,
                is UiState.TokenizationSuccessReceived,
                is UiState.CheckoutCompleted -> {
                    findNavController().navigate(
                        R.id.action_KlarnaFragment_to_HeadlessFragment
                    )
                }

                else -> Unit
            }
        }
    }

    companion object {
        const val PRIMER_SESSION_INTENT_ARG = "primerSessionIntent"
    }
}
