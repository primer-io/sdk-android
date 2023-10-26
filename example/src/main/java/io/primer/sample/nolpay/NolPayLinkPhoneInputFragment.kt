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
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.sample.databinding.FragmentNolPayPhoneFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NolPayLinkPhoneInputFragment : Fragment() {

    private lateinit var binding: FragmentNolPayPhoneFragmentBinding
    private lateinit var linkCardComponent: NolPayLinkCardComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayPhoneFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayLinkCardComponent(
                requireParentFragment().requireParentFragment()
            )

        binding.mobileNumber.doAfterTextChanged {
            linkCardComponent.updateCollectedData(
                NolPayLinkCollectableData.NolPayPhoneData(it.toString())
            )
        }

        binding.nextButton.setOnClickListener {
            linkCardComponent.submit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                linkCardComponent.componentValidationStatus.collectLatest { validationStatus ->
                    binding.mobileNumber.error = null
                    when (validationStatus) {
                        is PrimerValidationStatus.Valid -> {
                            binding.nextButton.isEnabled = true
                            binding.progressBar.isVisible = false
                        }

                        is PrimerValidationStatus.Invalid -> {
                            binding.nextButton.isEnabled = validationStatus.validationErrors.isEmpty()
                            binding.progressBar.isVisible = false
                            binding.mobileNumber.error =
                                validationStatus.validationErrors.firstOrNull()?.description
                        }

                        is PrimerValidationStatus.Validating -> {
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = true
                        }

                        is PrimerValidationStatus.Error -> {
                            binding.mobileNumber.error = validationStatus.error.description
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            }
        }
    }
}
