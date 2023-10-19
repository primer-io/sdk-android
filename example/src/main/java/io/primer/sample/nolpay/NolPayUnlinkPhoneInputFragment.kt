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
                    it.toString(),
                    binding.mobileCountryCode.text.toString()
                )
            )
        }

        binding.nextButton.setOnClickListener {
            unlinkCardComponent.submit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unlinkCardComponent.componentValidationErrors.collectLatest {
                    binding.nextButton.isEnabled = it.isEmpty()
                }
            }
        }
    }
}
