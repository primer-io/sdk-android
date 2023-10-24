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
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.sample.databinding.FragmentNolPayPhoneFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NolPayUnlinkPhoneInputFragment : Fragment() {

    private lateinit var binding: FragmentNolPayPhoneFragmentBinding
    private lateinit var unlinkCardComponent: NolPayUnlinkCardComponent

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
        unlinkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayUnlinkCardComponent(
                requireParentFragment().requireParentFragment()
            )

        binding.mobileNumber.doAfterTextChanged {
            unlinkCardComponent.updateCollectedData(
                NolPayUnlinkCollectableData.NolPayCardAndPhoneData(
                    requireParentFragment().requireParentFragment().requireArguments()
                        ?.getSerializable(
                            NolFragment.NOL_CARD_KEY
                        ) as PrimerNolPaymentCard,
                    binding.mobileCountryCode.text.toString().plus(it.toString()),
                )
            )
        }

        binding.nextButton.setOnClickListener {
            unlinkCardComponent.submit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unlinkCardComponent.componentValidationStatus.collectLatest { validationStatus ->
                    binding.mobileNumber.error = null
                    when (validationStatus) {
                        is PrimerValidationStatus.Validated -> {
                            binding.nextButton.isEnabled = validationStatus.errors.isEmpty()
                            binding.progressBar.isVisible = false
                            binding.mobileNumber.error =
                                validationStatus.errors.firstOrNull()?.description
                        }

                       is PrimerValidationStatus.Validating -> {
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = true
                        }

                        is PrimerValidationStatus.Error -> {
                            binding.nextButton.isEnabled = false
                            binding.progressBar.isVisible = false
                            binding.mobileNumber.error = validationStatus.error.description
                        }
                    }
                }
            }
        }
    }
}
