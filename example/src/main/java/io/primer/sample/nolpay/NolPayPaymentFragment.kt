package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.nolpay.api.manager.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.nolpay.api.manager.payment.component.NolPayPaymentComponent
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentStep
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayPaymentBinding
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.utils.requireApplication
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.flow.collectLatest

class NolPayPaymentFragment : Fragment() {

    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel
    private lateinit var binding: FragmentNolPayPaymentBinding
    private lateinit var startPaymentComponent: NolPayPaymentComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headlessManagerViewModel = ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository(), requireApplication()),
        )[HeadlessManagerViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is UiState.ShowError -> findNavController().navigate(
                            R.id.action_NolPayPaymentFragment_to_NolFragment
                        )

                        else -> Unit
                    }
                }
            }
        }

        startPaymentComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayPaymentComponent(
                this
            )

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                startPaymentComponent.componentError.collectLatest {
                    Snackbar.make(requireView(), it.description, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                startPaymentComponent.componentStep.collectLatest { nolPayLinkStep ->
                    when (nolPayLinkStep) {
                        NolPayPaymentStep.CollectCardAndPhoneData -> {
                            Snackbar.make(
                                requireView(),
                                "Payment started, please wait for the next step!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            startPaymentComponent.updateCollectedData(
                                NolPayPaymentCollectableData.NolPayCardAndPhoneData(
                                    requireArguments().getSerializable(
                                        NolFragment.NOL_CARD_KEY
                                    ) as PrimerNolPaymentCard,
                                    requireArguments().getString(NolFragment.PHONE_NUMBER)
                                        .orEmpty()
                                )
                            ).also {
                                startPaymentComponent.submit()
                            }
                        }

                        NolPayPaymentStep.CollectTagData -> Snackbar.make(
                            requireView(),
                            "Scan Nol card!",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        NolPayPaymentStep.PaymentRequested -> {
                            Snackbar.make(
                                requireView(),
                                "Completed payment inside SDK!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                            findNavController().navigate(
                                R.id.action_NolPayPaymentFragment_to_NolFragment
                            )
                        }
                    }
                }
            }
        }

        startPaymentComponent.start()
    }
}
