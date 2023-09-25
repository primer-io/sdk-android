package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.sample.databinding.FragmentNolPayUnlinkOtpBinding
import kotlinx.coroutines.flow.collectLatest

class NolPayUnlinkOtpFragment : Fragment() {

    private lateinit var binding: FragmentNolPayUnlinkOtpBinding
    private lateinit var unlinkCardComponent: NolPayUnlinkCardComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayUnlinkOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unlinkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayUnlinkCardComponent(
                requireParentFragment().requireParentFragment()
            )

        binding.otpCode.doAfterTextChanged {
            unlinkCardComponent.updateCollectedData(
                NolPayUnlinkCollectableData.NolPayOtpData(
                    it.toString(),
                )
            )
            binding.nextButton.isEnabled = it?.length == 6
        }

        binding.nextButton.setOnClickListener {
            unlinkCardComponent.submit()
        }

        lifecycleScope.launchWhenCreated {
            unlinkCardComponent.validationErrors.collectLatest {
                binding.nextButton.isEnabled = it.isEmpty()
            }
        }
    }
}
