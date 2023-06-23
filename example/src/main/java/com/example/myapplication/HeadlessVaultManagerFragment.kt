package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.constants.PrimerHeadlessCallbacks
import com.example.myapplication.databinding.FragmentVaultManagerBinding
import com.example.myapplication.datamodels.CheckoutDataWithError
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.datamodels.toMappedError
import com.example.myapplication.repositories.AppApiKeyRepository
import com.example.myapplication.viewmodels.HeadlessManagerViewModel
import com.example.myapplication.viewmodels.HeadlessManagerViewModelFactory
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.UiState
import com.google.gson.GsonBuilder
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManager
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManagerInterface
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PromptPayCheckoutAdditionalInfo
import kotlinx.coroutines.launch

class HeadlessVaultManagerFragment : Fragment() {

    private val callbacks: ArrayList<String> = arrayListOf()
    private var checkoutDataWithError: CheckoutDataWithError? = null

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel

    private lateinit var vaultManager: PrimerHeadlessUniversalCheckoutVaultManagerInterface

    private lateinit var binding: FragmentVaultManagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentVaultManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headlessManagerViewModel = ViewModelProvider(
            this,
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]
        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (headlessManagerViewModel.isLaunched != true) {
                token?.let {
                    headlessManagerViewModel.isLaunched = true
                    headlessManagerViewModel.startHeadless(
                        requireContext(), token, viewModel.settings
                    )
                }
            } else {
                headlessManagerViewModel.setHeadlessListeners()
            }
        }

        headlessManagerViewModel.paymentMethodsLoaded.observe(viewLifecycleOwner) {
            hideLoading()
            setupVaultedPaymentMethods()
        }

        headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.InitializingHeadless -> showLoading("Initializing Headless.")
                is UiState.InitializedHeadless -> hideLoading()
                is UiState.TokenizationStarted -> {
                    callbacks.add(PrimerHeadlessCallbacks.ON_TOKENIZATION_STARTED)
                    showLoading("Tokenization started ${state.paymentMethodType}")
                }
                is UiState.PreparationStarted -> {
                    callbacks.add(PrimerHeadlessCallbacks.ON_PREPARATION_STARTED)
                    showLoading("Preparation started ${state.paymentMethodType}")
                }
                is UiState.PaymentMethodShowed -> showLoading("Presented ${state.paymentMethodType}")
                is UiState.TokenizationSuccessReceived -> {
                    showLoading("Tokenization success ${state.paymentMethodTokenData}. Creating payment.")
                    headlessManagerViewModel.createPayment(
                        state.paymentMethodTokenData,
                        requireNotNull(viewModel.environment.value),
                        viewModel.descriptor.value.orEmpty(),
                        state.decisionHandler
                    )
                }
                is UiState.ResumePaymentReceived -> {
                    showLoading("Resume success. Resuming payment.")
                    headlessManagerViewModel.resumePayment(
                        state.resumeToken,
                        requireNotNull(viewModel.environment.value),
                        state.decisionHandler
                    )
                }
                is UiState.ResumePendingReceived -> {
                    hideLoading()
                    when (state.additionalInfo) {
                        is MultibancoCheckoutAdditionalInfo -> {
                            AlertDialog.Builder(context)
                                .setMessage("MultibancoData: $state.additionalInfo")
                                .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                            Log.d(TAG, "onResumePending MULTIBANCO: $state.additionalInfo")
                        }
                    }
                }
                is UiState.AdditionalInfoReceived -> {
                    hideLoading()
                    when (state.additionalInfo) {
                        is PromptPayCheckoutAdditionalInfo -> {
                            AlertDialog.Builder(context)
                                .setMessage("PromptPay: $state.additionalInfo")
                                .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                            Log.d(TAG, "onAdditionalInfoReceived: $state.additionalInfo")
                        }
                    }
                }
                is UiState.BeforePaymentCreateReceived -> {
                    callbacks.add(PrimerHeadlessCallbacks.ON_BEFORE_PAYMENT_CREATED)
                }
                is UiState.BeforeClientSessionUpdateReceived -> {
                    callbacks.add(PrimerHeadlessCallbacks.ON_BEFORE_CLIENT_SESSION_UPDATED)
                }
                is UiState.ClientSessionUpdatedReceived -> {
                    callbacks.add(PrimerHeadlessCallbacks.ON_CLIENT_SESSION_UPDATED)
                }
                is UiState.ShowError -> {
                    checkoutDataWithError =
                        CheckoutDataWithError(state.payment, state.error.toMappedError())
                    callbacks.add(PrimerHeadlessCallbacks.ON_FAILED_WITH_CHECKOUT_DATA)
                    hideLoading()
                    navigateToResultScreen()
                }
                is UiState.CheckoutCompleted -> {
                    checkoutDataWithError = CheckoutDataWithError(state.checkoutData.payment)
                    callbacks.add(PrimerHeadlessCallbacks.ON_CHECKOUT_COMPLETED)
                    hideLoading()
                    navigateToResultScreen()
                }
            }
        }

        if (savedInstanceState == null) {
            viewModel.fetchClientSession()
            showLoading("Loading client token.")
        }

        headlessManagerViewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> headlessManagerViewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        binding.apply {
            cvvInput.doAfterTextChanged {
                val vaultedPaymentMethodId = vaultedPaymentMethodsGroup.children
                    .filter { it.id == vaultedPaymentMethodsGroup.checkedRadioButtonId }
                    .firstOrNull()?.tag?.toString()
                lifecycleScope.launch {
                    val errors = vaultedPaymentMethodId?.let { id ->
                        vaultManager.validate(
                            id,
                            PrimerVaultedCardAdditionalData(it.toString())
                        ).getOrThrow()
                    }
                    submitButton.isEnabled = errors.isNullOrEmpty() || it.toString().isBlank()
                }
            }
            submitButton.setOnClickListener {
                lifecycleScope.launch {
                    val vaultedPaymentMethodId = vaultedPaymentMethodsGroup.children
                        .filter { it.id == vaultedPaymentMethodsGroup.checkedRadioButtonId }
                        .first().tag.toString()
                    when (cvvInput.text.toString().isBlank()) {
                        true -> vaultManager.startPaymentFlow(vaultedPaymentMethodId)
                        false -> vaultManager.startPaymentFlow(
                            vaultedPaymentMethodId,
                            PrimerVaultedCardAdditionalData(cvvInput.text.toString())
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setClientToken(null)
    }

    private fun setupVaultedPaymentMethods() {
        vaultManager = PrimerHeadlessUniversalCheckoutVaultManager.newInstance()
        lifecycleScope.launch {
            showLoading("Loading vaulted payment methods.")
            val vaultedPaymentMethods =
                vaultManager.fetchVaultedPaymentMethods().getOrElse { emptyList() }.filter {
                    it.paymentMethodType == "PAYMENT_CARD"
                }
            vaultedPaymentMethods.map { vaultedMethod ->
                binding.vaultedPaymentMethodsGroup.addView(RadioButton(context).apply {
                    id = vaultedMethod.id.hashCode()
                    tag = vaultedMethod.id
                    text = getString(
                        R.string.last_four,
                        vaultedMethod.paymentInstrumentData.last4Digits
                    )
                    minimumHeight = resources.getDimensionPixelSize(R.dimen.pay_button_height)
                })
            }
            vaultedPaymentMethods.firstOrNull()?.id?.hashCode()
                ?.let { binding.vaultedPaymentMethodsGroup.check(it) }
            hideLoading()
        }
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.root.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.root.isVisible = false
    }

    private fun navigateToResultScreen() {
        headlessManagerViewModel.resetUiState()
        findNavController().navigate(R.id.action_VaultManagerFragment_to_MerchantResultFragment,
            Bundle().apply {
                putInt(
                    MerchantResultFragment.PAYMENT_STATUS_KEY,
                    if (checkoutDataWithError?.error != null) MerchantResultFragment.Companion.PaymentStatus.FAILURE.ordinal
                    else MerchantResultFragment.Companion.PaymentStatus.SUCCESS.ordinal
                )
                putStringArrayList(
                    MerchantResultFragment.INVOKED_CALLBACKS_KEY,
                    callbacks
                )
                putString(
                    MerchantResultFragment.PAYMENT_RESPONSE_KEY,
                    GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                        .toJson(checkoutDataWithError)
                )
            })
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}