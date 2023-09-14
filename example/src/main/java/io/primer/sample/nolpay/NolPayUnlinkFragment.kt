package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.manager.nolPay.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.NolPayUnlinkDataStep
import io.primer.android.components.manager.nolPay.composable.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.models.PrimerNolPaymentCard
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayUnlinkBinding
import kotlinx.coroutines.flow.collectLatest

class NolPayUnlinkFragment : Fragment() {

    private lateinit var binding: FragmentNolPayUnlinkBinding
    private lateinit var unlinkCardComponent: NolPayUnlinkCardComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayUnlinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.parentNavFragment) as NavHostFragment

        unlinkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayUnlinkCardComponent(
                this
            )

        lifecycleScope.launchWhenCreated {
            unlinkCardComponent.stepFlow.collectLatest {
                when (it) {
                    NolPayUnlinkDataStep.COLLECT_CARD_DATA -> unlinkCardComponent.updateCollectedData(
                        NolPayUnlinkCollectableData.NolPayCardData(PrimerNolPaymentCard("0313971137"))
                    ).also {
                        unlinkCardComponent.submit()
                    }

                    NolPayUnlinkDataStep.COLLECT_PHONE_DATA -> Unit
                    NolPayUnlinkDataStep.COLLECT_OTP_DATA -> navHostFragment.findNavController()
                        .navigate(R.id.action_NolPayPhoneInputFragment_to_NolPayUnlinkOtpFragment)

                    NolPayUnlinkDataStep.CARD_UNLINKED -> {
                        Snackbar.make(requireView(), "Successfully unlinked!", Snackbar.LENGTH_SHORT)
                            .show()
                        findNavController().navigate(R.id.action_NolUnlinkFragment_to_NolFragment)
                    }
                }
            }
        }

        unlinkCardComponent.start()
    }
}
