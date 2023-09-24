package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentCollectableData
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentComponent
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.components.manager.nolPay.nfc.component.NolPayNfcComponent
import io.primer.sample.databinding.FragmentNolCardLinkScanTagBinding
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest

class NolPayPaymentScanTagFragment : Fragment() {

    private lateinit var binding: FragmentNolCardLinkScanTagBinding
    private lateinit var startPaymentComponent: NolPayStartPaymentComponent
    private lateinit var nfcComponent: NolPayNfcComponent

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolCardLinkScanTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPaymentComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayStartPaymentComponent(
                requireParentFragment().requireParentFragment()
            )
        nfcComponent = PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayNfcComponent()

        lifecycleScope.launchWhenCreated {
            startPaymentComponent.stepFlow.collectLatest { step: NolPayStartPaymentStep ->
                when (step) {
                    NolPayStartPaymentStep.CollectStartPaymentData -> Unit
                    NolPayStartPaymentStep.CollectTagData -> Unit
                }
            }
        }

        mainViewModel.collectedTag.observe(viewLifecycleOwner) { intent ->
            nfcComponent.getAvailableTag(intent)?.let {
                startPaymentComponent.updateCollectedData(
                    NolPayStartPaymentCollectableData.NolPayTagData(
                        it
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcComponent.enableForegroundDispatch(requireActivity(), 1)
    }

    override fun onPause() {
        super.onPause()
        nfcComponent.disableForegroundDispatch(requireActivity())
    }
}
