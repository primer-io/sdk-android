package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.sample.databinding.FragmentNolPayUnlinkOtpBinding
import kotlinx.coroutines.flow.collectLatest


class NolPayLinkOtpFragment : Fragment() {

    private lateinit var binding: FragmentNolPayUnlinkOtpBinding
    private lateinit var linkCardComponent: NolPayLinkCardComponent
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
        linkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayLinkCardComponent(
                requireParentFragment().requireParentFragment()
            )

        binding.otpCode.doAfterTextChanged {
            linkCardComponent.updateCollectedData(
                NolPayLinkCollectableData.NolPayOtpData(
                    it.toString(),
                )
            )
            binding.nextButton.isEnabled = it?.length == 6
        }

        binding.nextButton.setOnClickListener {
            linkCardComponent.submit()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                linkCardComponent.componentValidationErrors.collectLatest {
                    binding.nextButton.isEnabled = it.isEmpty()
                }
            }
        }
    }
}
