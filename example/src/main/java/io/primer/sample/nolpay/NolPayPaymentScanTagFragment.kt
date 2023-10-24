package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.components.manager.nolPay.nfc.component.NolPayNfcComponent
import io.primer.sample.databinding.FragmentNolCardLinkScanTagBinding
import io.primer.sample.viewmodels.MainViewModel

class NolPayPaymentScanTagFragment : Fragment() {

    private lateinit var binding: FragmentNolCardLinkScanTagBinding
    private lateinit var startPaymentComponent: NolPayPaymentComponent
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
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayPaymentComponent(
                requireParentFragment().requireParentFragment()
            )
        nfcComponent = PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayNfcComponent()

        mainViewModel.collectedTag.observe(viewLifecycleOwner) { intent ->
            nfcComponent.getAvailableTag(intent)?.let {
                startPaymentComponent.updateCollectedData(
                    NolPayPaymentCollectableData.NolPayTagData(
                        it
                    )
                ).also {
                    startPaymentComponent.submit()
                }
                mainViewModel.setTag(null)
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
