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
import io.primer.android.components.manager.nolPay.NolPayData
import io.primer.android.components.manager.nolPay.NolPayCollectDataStep
import io.primer.android.components.manager.nolPay.NolPayIntent
import io.primer.android.components.manager.nolPay.NolPayResult
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniveralCheckoutNolPayManager
import io.primer.nolpay.PrimerNolPayNfcUtils
import io.primer.nolpay.models.PrimerNolPaymentCard
import io.primer.sample.databinding.FragmentNolPayBinding
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NolFragment : Fragment() {

    private lateinit var binding: FragmentNolPayBinding
    private lateinit var manager: PrimerHeadlessUniveralCheckoutNolPayManager
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
        manager = PrimerHeadlessUniveralCheckoutNolPayManager.getInstance(requireActivity())

        mainViewModel.collectedTag.observe(viewLifecycleOwner) { tag ->
            tag?.let { manager.updateCollectedData(NolPayData.NolPayTagData(it)) }
        }
        binding.mobileNumber.doAfterTextChanged {
            manager.updateCollectedData(
                NolPayData.NolPayPhoneData(
                    it.toString().trim(),
                    binding.mobileCountryCode.text.toString().trim()
                )
            )
        }

        binding.otpCode.doAfterTextChanged {
            manager.updateCollectedData(NolPayData.NolPayOtpData(it.toString().trim()))
        }

        binding.nextButton.setOnClickListener {
            binding.progressIndicator.isVisible = true
            it.isEnabled = false
            manager.submit()
        }
        // we are just simulating navigation here
        lifecycleScope.launch {
            combine(
                manager.collectDataStepFlow,
                manager.validationFlow
            ) { nolPayCollectDataStep: NolPayCollectDataStep, primerValidationErrors: List<PrimerValidationError> ->
                Pair(nolPayCollectDataStep, primerValidationErrors)
            }.collectLatest { pair ->
                when (pair.first) {
                    NolPayCollectDataStep.COLLECT_TAG_DATA -> {
                        //  display UI to scan tag
                        binding.tagGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty()
                    }

                    NolPayCollectDataStep.COLLECT_PHONE_DATA -> {
                        //  display UI to enter mobile number
                        binding.tagGroup.isVisible = false
                        binding.linkCardGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty() &&
                            binding.mobileNumber.text.toString().isNotBlank()

                        pair.second.find { it.errorId == "invalid-phone-number" }?.let {
                            binding.mobileNumber.error = it.description
                        } ?: kotlin.run { binding.mobileNumber.error = null }
                    }

                    NolPayCollectDataStep.COLLECT_OTP_DATA -> {
                        // display UI to enter OTP
                        binding.progressIndicator.isVisible = false
                        binding.nextButton.isEnabled = pair.second.isEmpty()
                        binding.linkCardGroup.isVisible = false
                        binding.otpGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty() &&
                            binding.otpCode.text.toString().isNotBlank()
                        pair.second.find { it.errorId == "invalid-otp-code" }?.let {
                            binding.otpCode.error = it.description
                        } ?: kotlin.run { binding.otpCode.error = null }
                    }
                }
            }
        }

        lifecycleScope.launch {
            manager.errorFlow.collectLatest {
                Snackbar.make(requireView(), it.description, Snackbar.LENGTH_LONG).show()
            }
        }

        manager.start(NolPayIntent.LinkPaymentCard)
    }

    override fun onResume() {
        super.onResume()
        PrimerNolPayNfcUtils.enableForegroundDispatch(requireActivity(), 1)
    }

    override fun onPause() {
        super.onPause()
        PrimerNolPayNfcUtils.disableForegroundDispatch(requireActivity())
    }

    fun implementation() {
        // developer can observe different types of events and combine them if needed
        // NolPayHeadlessManager will instruct developer for steps needed to complete initiated flow
        // this saves us from having to define each flow and developer can implement navigation handling only once
        lifecycleScope.launch {
            combine(
                manager.collectDataStepFlow,
                manager.validationFlow
            ) { nolPayCollectDataStep: NolPayCollectDataStep, primerValidationErrors: List<PrimerValidationError> ->
                Pair(nolPayCollectDataStep, primerValidationErrors)
            }.collectLatest { pair ->
                when (pair.first) {
                    NolPayCollectDataStep.COLLECT_TAG_DATA -> {
                        //  display UI to scan tag
                    }

                    NolPayCollectDataStep.COLLECT_PHONE_DATA -> {
                        //  display UI to enter mobile number and validate the phone number
                    }

                    NolPayCollectDataStep.COLLECT_OTP_DATA -> {
                        // display UI to enter OTP and listen to specific OTP validation errors
                    }
                }
            }
        }

        lifecycleScope.launch {
            manager.errorFlow.collectLatest {
                // in case we encounter errors during the flow that are not tokenization or payment related
                // developer can display errors to the user, as in most cases user can recover or take further actions
                // for example scanning tag went wrong, network error etc
            }
        }

        lifecycleScope.launch {
            manager.resultFlow.collectLatest {nolPayResult ->
                when(nolPayResult) {
                    is NolPayResult.PaymentCardLinked -> // display message to user and navigate
                    is NolPayResult.PaymentCardUnlinked -> // display message to user and navigate
                    is NolPayResult.PaymentFlowStarted -> // listen to PrimerHeadlessUniversalCheckoutListener
                }
            }
        }


        // developer calls NolPayHeadlessManager with specific intent

        // 1. card linking
        manager.start(NolPayIntent.LinkPaymentCard)

        // 2. card unlinking, "List<NolPaymentCard>" are current linked cards
        manager.start(NolPayIntent.UnlinkPaymentCard(PrimerNolPaymentCard("33333")))

        // 3. creating payment, "List<NolPaymentCard>" are current linked cards
        manager.start(NolPayIntent.StartPaymentFlow(PrimerNolPaymentCard("33333")))
    }
}