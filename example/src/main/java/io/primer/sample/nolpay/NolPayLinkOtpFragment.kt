package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.nolpay.api.manager.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.android.nolpay.api.manager.linkCard.component.NolPayLinkCardComponent
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
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
                linkCardComponent.componentValidationStatus.collectLatest { validationStatus ->
                    binding.otpCode.error = null
                    when (validationStatus) {
                        is PrimerValidationStatus.Valid -> {
                            binding.nextButton.isEnabled = true
                            binding.progressBar.isVisible = false
                        }

                        is PrimerValidationStatus.Invalid -> {
                            binding.nextButton.isEnabled = validationStatus.validationErrors.isEmpty()
                            binding.otpCode.error =
                                validationStatus.validationErrors.firstOrNull()?.description
                            binding.progressBar.isVisible = false
                        }

                        is PrimerValidationStatus.Validating -> {
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = true
                        }

                        is PrimerValidationStatus.Error -> {
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = false
                            binding.otpCode.error = validationStatus.error.description
                        }
                    }
                }
            }
        }
    }
}
