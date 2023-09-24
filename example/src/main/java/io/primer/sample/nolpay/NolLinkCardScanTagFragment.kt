package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.components.manager.nolPay.nfc.component.NolPayNfcComponent
import io.primer.nolpay.api.PrimerNolPayNfcUtils
import io.primer.sample.databinding.FragmentNolCardLinkScanTagBinding
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class NolLinkCardScanTagFragment : Fragment() {

    private lateinit var binding: FragmentNolCardLinkScanTagBinding
    private lateinit var linkCardComponent: NolPayLinkCardComponent
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
        linkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayLinkCardComponent(
                requireParentFragment().requireParentFragment()
            )
        nfcComponent = PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayNfcComponent()

        lifecycleScope.launchWhenCreated {
            combine(linkCardComponent.validationFlow, linkCardComponent.stepFlow) { a, b ->
                Pair(a, b)
            }.collectLatest {
                if (it.first.isEmpty() && it.second == NolPayLinkCardStep.CollectTagData)
                    linkCardComponent.submit()
            }
        }

        mainViewModel.collectedTag.observe(viewLifecycleOwner) { intent ->
            nfcComponent.getAvailableTag(intent)?.let { tag ->
                linkCardComponent.updateCollectedData(
                    NolPayLinkCollectableData.NolPayTagData(tag)
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
