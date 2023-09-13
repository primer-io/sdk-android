package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.nolPay.NolPayLinkDataStep
import io.primer.android.components.manager.nolPay.NolPayLinkCardComponent
import io.primer.android.components.manager.nolPay.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.NolPayUnlinkDataStep
import io.primer.android.components.manager.nolPay.composable.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.PrimerNolPayNfcUtils
import io.primer.nolpay.models.PrimerNolPaymentCard
import io.primer.sample.databinding.FragmentNolPayBinding
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NolFragment : Fragment() {

    private lateinit var binding: FragmentNolPayBinding
    private lateinit var manager: PrimerHeadlessUniversalCheckoutNolPayManager
    private lateinit var linkCardComponent: NolPayLinkCardComponent
    private lateinit var unlinkCardComponent: NolPayUnlinkCardComponent
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = PrimerHeadlessUniversalCheckoutNolPayManager()
        linkCardComponent = manager.provideNolPayLinkCardComponent(requireActivity())
        unlinkCardComponent = manager.provideNolPayUnlinkCardComponent(requireActivity())

        lifecycleScope.launch {
            manager.provideNolPayLinkedCardsComponent().getLinkedCards("62250843", "387")
                .onSuccess {
                    println(it)
                }
        }
        mainViewModel.collectedTag.observe(viewLifecycleOwner) { tag ->
            tag?.let {
                linkCardComponent.updateCollectedData(
                    NolPayLinkCollectableData.NolPayTagData(
                        it
                    )
                )
            }
        }
        binding.mobileNumber.doAfterTextChanged {
            linkCardComponent.updateCollectedData(
                NolPayLinkCollectableData.NolPayPhoneData(
                    it.toString().trim(), binding.mobileCountryCode.text.toString().trim()
                )
            )
        }

        binding.otpCode.doAfterTextChanged {
            linkCardComponent.updateCollectedData(
                NolPayLinkCollectableData.NolPayOtpData(
                    it.toString().trim()
                )
            )
        }

        binding.nextButton.setOnClickListener {
            binding.progressIndicator.isVisible = true
            it.isEnabled = false
            linkCardComponent.submit()
        }
        // we are just simulating navigation here
        lifecycleScope.launch {
            combine(
                linkCardComponent.stepFlow, linkCardComponent.validationFlow
            ) { nolPayCollectDataStep: NolPayLinkDataStep, primerValidationErrors: List<PrimerValidationError> ->
                Pair(nolPayCollectDataStep, primerValidationErrors)
            }.collectLatest { pair ->
                when (pair.first) {
                    NolPayLinkDataStep.COLLECT_TAG_DATA -> {
                        //  display UI to scan tag
                        binding.tagGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty()
                    }

                    NolPayLinkDataStep.COLLECT_PHONE_DATA -> {
                        //  display UI to enter mobile number
                        binding.tagGroup.isVisible = false
                        binding.linkCardGroup.isVisible = true
                        binding.nextButton.isEnabled =
                            pair.second.isEmpty() && binding.mobileNumber.text.toString()
                                .isNotBlank()

                        pair.second.find { it.errorId == "invalid-phone-number" }?.let {
                            binding.mobileNumber.error = it.description
                        } ?: kotlin.run { binding.mobileNumber.error = null }
                    }

                    NolPayLinkDataStep.COLLECT_OTP_DATA -> {
                        // display UI to enter OTP
                        binding.progressIndicator.isVisible = false
                        binding.nextButton.isEnabled = pair.second.isEmpty()
                        binding.linkCardGroup.isVisible = false
                        binding.otpGroup.isVisible = true
                        binding.nextButton.isEnabled =
                            pair.second.isEmpty() && binding.otpCode.text.toString().isNotBlank()
                        pair.second.find { it.errorId == "invalid-otp-code" }?.let {
                            binding.otpCode.error = it.description
                        } ?: kotlin.run { binding.otpCode.error = null }
                    }

                    NolPayLinkDataStep.CARD_LINKED -> TODO()
                }
            }
        }

        lifecycleScope.launch {
            linkCardComponent.errorFlow.collectLatest {
                Snackbar.make(requireView(), it.description, Snackbar.LENGTH_LONG).show()
            }
        }

        linkCardComponent.start()

        lifecycleScope.launch {
            unlinkCardComponent.stepFlow.collectLatest {
                when (it) {
                    NolPayUnlinkDataStep.COLLECT_CARD_DATA -> unlinkCardComponent.updateCollectedData(
                        NolPayUnlinkCollectableData.NolPayCardData(
                            PrimerNolPaymentCard("0313971137")
                        )
                    ).also {
                        unlinkCardComponent.submit()
                    }

                    NolPayUnlinkDataStep.COLLECT_PHONE_DATA -> unlinkCardComponent.updateCollectedData(
                        NolPayUnlinkCollectableData.NolPayPhoneData(
                            "62250843", "387"
                        )
                    ).also {
                        unlinkCardComponent.submit()
                    }

                    NolPayUnlinkDataStep.COLLECT_OTP_DATA -> unlinkCardComponent.updateCollectedData(
                        NolPayUnlinkCollectableData.NolPayOtpData(
                            "987123"
                        )
                    ).also {
                        unlinkCardComponent.submit()
                    }

                    NolPayUnlinkDataStep.CARD_UNLINKED -> Snackbar.make(
                        requireView(), "Yes!", Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // unlinkCardComponent.start()
    }

    override fun onResume() {
        super.onResume()
        PrimerNolPayNfcUtils.enableForegroundDispatch(requireActivity(), 1)
    }

    override fun onPause() {
        super.onPause()
        PrimerNolPayNfcUtils.disableForegroundDispatch(requireActivity())
    }
}