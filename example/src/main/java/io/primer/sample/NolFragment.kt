package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.nolPay.NolPayData
import io.primer.android.components.manager.nolPay.NolPayStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessNolPayManager
import io.primer.sample.databinding.FragmentNolPayBinding
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NolFragment : Fragment() {

    private lateinit var binding: FragmentNolPayBinding
    private lateinit var manager: PrimerHeadlessNolPayManager
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
        manager = PrimerHeadlessNolPayManager.getInstance(requireActivity())

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
                manager.stepFlow,
                manager.validationFlow
            ) { nolPayStep: NolPayStep, primerValidationErrors: List<PrimerValidationError> ->
                Pair(nolPayStep, primerValidationErrors)
            }.collectLatest { pair ->
                when (pair.first) {
                    NolPayStep.COLLECT_TAG_DATA -> {
                        //  display UI to scan tag
                        binding.tagGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty()
                    }

                    NolPayStep.COLLECT_PHONE_DATA -> {
                        //  display UI to enter mobile number
                        binding.tagGroup.isVisible = false
                        binding.linkCardGroup.isVisible = true
                        binding.nextButton.isEnabled = pair.second.isEmpty() &&
                            binding.mobileNumber.text.toString().isNotBlank()

                        pair.second.find { it.errorId == "invalid-phone-number" }?.let {
                            binding.mobileNumber.error = it.description
                        } ?: kotlin.run { binding.mobileNumber.error = null }
                    }

                    NolPayStep.COLLECT_OTP_DATA -> {
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

                    NolPayStep.PAYMENT_TOKENIZED -> {
                        Toast.makeText(
                            requireContext(),
                            "Tokenization Successful!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            manager.errorFlow.collectLatest {
                Snackbar.make(requireView(), it.description, Snackbar.LENGTH_LONG).show()
            }
        }

        manager.start(PrimerSessionIntent.VAULT)
    }

    override fun onResume() {
        super.onResume()
        manager.enableForegroundDispatch(requireActivity(), 1)
    }

    override fun onPause() {
        super.onPause()
        manager.disableForegroundDispatch(requireActivity())
    }
}