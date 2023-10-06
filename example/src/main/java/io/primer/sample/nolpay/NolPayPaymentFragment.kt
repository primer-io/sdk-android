package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayPaymentBinding
import kotlinx.coroutines.flow.collectLatest

class NolPayPaymentFragment : Fragment() {

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

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.parentNavFragment) as NavHostFragment

        startPaymentComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayPaymentComponent(
                this
            )

        lifecycleScope.launchWhenCreated {
            startPaymentComponent.componentStep.collectLatest { nolPayLinkStep ->
                when (nolPayLinkStep) {
                    NolPayPaymentStep.CollectPaymentData ->
                        startPaymentComponent.updateCollectedData(
                            NolPayPaymentCollectableData.NolPayStartPaymentData(
                                requireArguments().getSerializable(
                                    NolFragment.NOL_CARD_KEY
                                ) as PrimerNolPaymentCard,
                                "62250843",
                                "387"
                            )
                        ).also {
                            startPaymentComponent.submit()
                        }

                    NolPayPaymentStep.CollectTagData -> Unit
                }
            }
        }

        startPaymentComponent.start()
    }
}
