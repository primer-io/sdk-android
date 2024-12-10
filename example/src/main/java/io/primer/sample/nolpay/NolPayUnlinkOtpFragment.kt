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
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.nolpay.api.manager.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.nolpay.api.manager.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.sample.databinding.FragmentNolPayUnlinkOtpBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        }

        binding.nextButton.setOnClickListener {
            unlinkCardComponent.submit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unlinkCardComponent.componentValidationStatus.collectLatest {
                    binding.nextButton.isEnabled = it is PrimerValidationStatus.Valid
                }
            }
        }
    }
}
